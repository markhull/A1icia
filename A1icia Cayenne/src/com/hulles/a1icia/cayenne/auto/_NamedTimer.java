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
package com.hulles.a1icia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

/**
 * Class _NamedTimer was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _NamedTimer extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String TIMER_ID_PK_COLUMN = "timer_ID";

    public static final Property<Long> DURATION_MS = Property.create("durationMs", Long.class);
    public static final Property<String> NAME = Property.create("name", String.class);
    public static final Property<String> TIMER_UUID = Property.create("timerUuid", String.class);

    protected long durationMs;
    protected String name;
    protected String timerUuid;


    public void setDurationMs(Long durationMs) {
        beforePropertyWrite("durationMs", this.durationMs, durationMs);
        this.durationMs = durationMs;
    }

    public Long getDurationMs() {
        beforePropertyRead("durationMs");
        return this.durationMs;
    }

    public void setName(String name) {
        beforePropertyWrite("name", this.name, name);
        this.name = name;
    }

    public String getName() {
        beforePropertyRead("name");
        return this.name;
    }

    public void setTimerUuid(String timerUuid) {
        beforePropertyWrite("timerUuid", this.timerUuid, timerUuid);
        this.timerUuid = timerUuid;
    }

    public String getTimerUuid() {
        beforePropertyRead("timerUuid");
        return this.timerUuid;
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "durationMs":
                return this.durationMs;
            case "name":
                return this.name;
            case "timerUuid":
                return this.timerUuid;
            default:
                return super.readPropertyDirectly(propName);
        }
    }

    @Override
    public void writePropertyDirectly(String propName, Object val) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch (propName) {
            case "durationMs":
                this.durationMs = val == null ? 0 : (long)val;
                break;
            case "name":
                this.name = (String)val;
                break;
            case "timerUuid":
                this.timerUuid = (String)val;
                break;
            default:
                super.writePropertyDirectly(propName, val);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        writeSerialized(out);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        readSerialized(in);
    }

    @Override
    protected void writeState(ObjectOutputStream out) throws IOException {
        super.writeState(out);
        out.writeLong(this.durationMs);
        out.writeObject(this.name);
        out.writeObject(this.timerUuid);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.durationMs = in.readLong();
        this.name = (String)in.readObject();
        this.timerUuid = (String)in.readObject();
    }

}