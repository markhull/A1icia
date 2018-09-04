/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
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

package com.hulles.alixia.webx.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.hulles.alixia.prong.shared.ProngException;
import com.hulles.alixia.prong.shared.SerialProng;
import com.hulles.alixia.webx.shared.SerialConsoleIn;
import com.hulles.alixia.webx.shared.SerialConsoleOut;
import com.hulles.alixia.webx.shared.SerialSystemInfo;

/**
 *
 * @author hulles
 */
@RemoteServiceRelativePath("services/mindservice")
public interface MindService extends RemoteService {
    SerialSystemInfo checkSystems();
	void pingProng(SerialProng prong) throws ProngException;
	void clearProng(SerialProng prong);
    void sendConsole(SerialProng prong, SerialConsoleIn input) throws ProngException;
    SerialConsoleOut receiveConsole(SerialProng prong) throws ProngException;
    String queryHealth(SerialProng prong) throws ProngException;
}
