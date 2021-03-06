package com.hulles.alixia.delta.deepspeech.swig;

/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.1
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */


public class Metadata {
    private transient long swigCPtr;
    protected transient boolean swigCMemOwn;

    protected Metadata(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(Metadata obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    @Override
    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                implJNI.delete_Metadata(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    public void setItems(MetadataItem value) {
        implJNI.Metadata_items_set(swigCPtr, this, MetadataItem.getCPtr(value), value);
    }

    public MetadataItem getItems() {
        long cPtr = implJNI.Metadata_items_get(swigCPtr, this);
        return (cPtr == 0) ? null : new MetadataItem(cPtr, false);
    }

    public void setNum_items(int value) {
        implJNI.Metadata_num_items_set(swigCPtr, this, value);
    }

    public int getNum_items() {
        return implJNI.Metadata_num_items_get(swigCPtr, this);
    }

    public void setConfidence(double value) {
        implJNI.Metadata_confidence_set(swigCPtr, this, value);
    }

    public double getConfidence() {
        return implJNI.Metadata_confidence_get(swigCPtr, this);
    }

    public MetadataItem getItem(int i) {
        return new MetadataItem(implJNI.Metadata_getItem(swigCPtr, this, i), true);
    }

}
