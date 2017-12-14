/*******************************************************************************
 * Copyright © 2017 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.hulles.a1icia.bravo;

import com.hulles.a1icia.api.shared.ApplicationKeys;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.DataType;
import org.tensorflow.Graph;
import org.tensorflow.Output;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.tools.A1iciaUtils;

/** 
 * Sample use of the TensorFlow Java API to label images using a pre-trained model.
 * 
 */
public class InceptionLabeller {
	private final static String INCEPTIONGRAPHURL = "/tensorflow_inception_graph.pb";
	private final static String LABELSTRINGSURL = "/imagenet_comp_graph_label_strings.txt";
	
	public static String analyzeImage(byte[] imageBytes) {
		byte[] graphDef;
		List<String> labels;
		float[] labelProbabilities;
		int bestLabelIdx;
		String resultStr = null;
		URI modelURI;
		URI labelsURI;
		ApplicationKeys appKeys;
        String inceptionPath;
        
        A1iciaUtils.checkNotNull(imageBytes);
        appKeys = ApplicationKeys.getInstance();
        inceptionPath = appKeys.getInceptionPath();
		try {
			modelURI = new URI(inceptionPath + INCEPTIONGRAPHURL);
			labelsURI = new URI(inceptionPath + LABELSTRINGSURL);
		} catch (URISyntaxException e) {
			throw new A1iciaException("Can't create URIs in InceptionLabeller", e);
		}
		graphDef = readAllBytes(Paths.get(modelURI));
		labels = readAllLines(Paths.get(labelsURI));
		try (Tensor image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
			labelProbabilities = executeInceptionGraph(graphDef, image);
			bestLabelIdx = maxIndex(labelProbabilities);
			resultStr = String.format("BEST MATCH: %s (%.2f%% likely)", labels.get(bestLabelIdx), 
					labelProbabilities[bestLabelIdx] * 100f);
		}
		return resultStr;
	}

	private static Tensor constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) {
		GraphBuilder builder;
		final int height;
		final int width;
		final float mean;
		final float scale;
		final Output input;
		final Output output;
		
		try (Graph graph = new Graph()) {
			builder = new GraphBuilder(graph);
			// Some constants specific to the pre-trained model at:
			// https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
			//
			// - The model was trained with images scaled to 224x224 pixels.
			// - The colors, represented as R, G, B in 1-byte each were converted to
			//   float using (value - Mean)/Scale.
			height = 224;
			width = 224;
			mean = 117f;
			scale = 1f;

			// Since the graph is being constructed once per execution here, we can use a constant for the
			// input image. If the graph were to be re-used for multiple input images, a placeholder would
			// have been more appropriate.
			input = builder.constant("input", imageBytes);
			output =
					builder.div(
						builder.sub(
							builder.resizeBilinear(
								builder.expandDims(
									builder.cast(builder.decodeJpeg(input, 3), DataType.FLOAT),
									builder.constant("make_batch", 0)),
								builder.constant("size", new int[] {height, width})),
						builder.constant("mean", mean)),
					builder.constant("scale", scale));
			try (Session session = new Session(graph)) {
				return session.runner().fetch(output.op().name()).run().get(0);
			}
		}
	}

	private static float[] executeInceptionGraph(byte[] graphDef, Tensor image) {
		final long[] rshape;
		String errStr;
		int nlabels;
		float[] floatResult;
		
		try (Graph graph = new Graph()) {
			graph.importGraphDef(graphDef);
			try (Session session = new Session(graph); 
					Tensor result = session.runner().feed("input", image).fetch("output").run().get(0)) {
				rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) {
					errStr = String.format(
							"Expected model to produce a [1 N] shaped tensor where N is the " +
							"number of labels, instead it produced one with shape %s",
							Arrays.toString(rshape));
					throw new RuntimeException(errStr);
				}
				nlabels = (int) rshape[1];
				floatResult = result.copyTo(new float[1][nlabels])[0];
				return floatResult;
			}
		}
	}

	private static int maxIndex(float[] probabilities) {
		int best;
		
		best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}

	static byte[] readAllBytes(Path path) {
		byte[] bytes = null;
		
		try {
			bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			throw new A1iciaException("Failed to read [" + path + "]: " + e.getMessage());
		}
		return bytes;
	}

	private static List<String> readAllLines(Path path) {
		List<String> lines = null;
		
		try {
			lines = Files.readAllLines(path, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new A1iciaException("Failed to read [" + path + "]: " + e.getMessage());
		}
		return lines;
	}

	// In the fullness of time, equivalents of the methods of this class should be auto-generated from
	// the OpDefs linked into libtensorflow_jni.so. That would match what is done in other languages
	// like Python, C++ and Go.
	static class GraphBuilder {
		private final Graph graph;
		
		GraphBuilder(Graph g) {
			
			this.graph = g;
		}

		Output div(Output x, Output y) {
			
			return binaryOp("Div", x, y);
		}

		Output sub(Output x, Output y) {
			
			return binaryOp("Sub", x, y);
		}

		Output resizeBilinear(Output images, Output size) {
			
			return binaryOp("ResizeBilinear", images, size);
		}

		Output expandDims(Output input, Output dim) {
			
			return binaryOp("ExpandDims", input, dim);
		}

		Output cast(Output value, DataType dtype) {
			
			return graph.opBuilder("Cast", "Cast")
					.addInput(value)
					.setAttr("DstT", dtype)
					.build()
					.output(0);
		}

		Output decodeJpeg(Output contents, long channels) {
			
			return graph.opBuilder("DecodeJpeg", "DecodeJpeg")
					.addInput(contents)
					.setAttr("channels", channels)
					.build()
					.output(0);
		}

		Output constant(String name, Object value) {
			
			try (Tensor t = Tensor.create(value)) {
				return graph.opBuilder("Const", name)
						.setAttr("dtype", t.dataType())
						.setAttr("value", t)
						.build()
						.output(0);
			}
		}

		private Output binaryOp(String type, Output in1, Output in2) {
			
			return graph.opBuilder(type, type)
					.addInput(in1)
					.addInput(in2)
					.build()
					.output(0);
		}
	}
}
