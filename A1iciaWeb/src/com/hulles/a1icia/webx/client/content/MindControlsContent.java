/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
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
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.hulles.a1icia.webx.client.content;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.hulles.a1icia.prong.shared.ProngException;
import com.hulles.a1icia.webx.client.A1iciaClientUtils;
import com.hulles.a1icia.webx.client.services.MindServiceEvent;
import com.hulles.a1icia.webx.client.services.ServiceHandler;
import com.hulles.a1icia.webx.client.services.ServiceHandler.MindServices;
import com.hulles.a1icia.webx.shared.SharedUtils;

/**
 *
 * @author hulles
 */
final class MindControlsContent {
    private static final String DEFAULT_STYLE_NAME = "ControlsContent";
    ScrollPanel messagePanel;
    private Button foxtrotButton;

    MindControlsContent() {
        
    }

    public Widget createWidget() {
        HorizontalPanel panel;
        FlexTable layout;
        FlexCellFormatter formatter;
 
        panel = new HorizontalPanel();
        panel.setSpacing(20);

        messagePanel = new ScrollPanel();
        messagePanel.setSize("600px", "400px");
        messagePanel.addStyleName(DEFAULT_STYLE_NAME + "-messagePanel");

        layout = new FlexTable();
        layout.setCellSpacing(6);
        formatter = layout.getFlexCellFormatter();

        foxtrotButton = new Button("system health", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
            	systemHealth();
            }
        });
        addRow(layout, formatter, "Query system health", foxtrotButton);
        
        panel.add(layout);
        panel.add(messagePanel);
        return panel;
    }

    static int addRow(FlexTable table, FlexCellFormatter formatter, String label, Widget widget) {
        HTML labelWidget;
        int row;

    	SharedUtils.checkNotNull(table);
    	SharedUtils.checkNotNull(formatter);
    	SharedUtils.checkNotNull(widget);
    	SharedUtils.checkNotNull(label);
        row = table.getRowCount();
        labelWidget = new HTML(label);
        labelWidget.setStylePrimaryName(DEFAULT_STYLE_NAME + "-formLabel");
        table.setWidget(row, 0, labelWidget);
        table.setWidget(row, 1, widget);
        return row;
    }
    
    void systemHealth() {
    	MindServiceEvent<String> event;

        messagePanel.setWidget(A1iciaClientUtils.getLoadingImage());
        final AsyncCallback<String> callback = new AsyncCallback<String>() {
            @Override
            public void onSuccess(String result) {
                messagePanel.clear();
                messagePanel.setWidget(new HTML(result));
            }
            @Override
            public void onFailure(Throwable caught) {
                messagePanel.clear();
		        if (caught instanceof ProngException) {
		        	A1iciaClientUtils.prongError(caught);
		        } else {
		        	A1iciaClientUtils.commError(caught);
		        }
            }
        };
 		event = new MindServiceEvent<>(MindServices.QUERYHEALTH);
		event.setCallback(callback);
		ServiceHandler.handleMindServiceEvent(event);
    }

}
