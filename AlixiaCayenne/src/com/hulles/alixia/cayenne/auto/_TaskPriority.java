package com.hulles.alixia.cayenne.auto;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.cayenne.BaseDataObject;
import org.apache.cayenne.exp.Property;

import com.hulles.alixia.cayenne.Task;

/**
 * Class _TaskPriority was generated by Cayenne.
 * It is probably a good idea to avoid changing this class manually,
 * since it may be overwritten next time code is regenerated.
 * If you need to make any customizations, please use subclass.
 */
public abstract class _TaskPriority extends BaseDataObject {

    private static final long serialVersionUID = 1L; 

    public static final String TASK_PRIORITY_ID_PK_COLUMN = "task_priority_ID";

    public static final Property<String> DESCRIPTION = Property.create("description", String.class);
    public static final Property<Short> SEQUENCE = Property.create("sequence", Short.class);
    public static final Property<String> TASK_PRIORITY_UUID = Property.create("taskPriorityUuid", String.class);
    public static final Property<List<Task>> TASKS = Property.create("tasks", List.class);

    protected String description;
    protected Short sequence;
    protected String taskPriorityUuid;

    protected Object tasks;

    public void setDescription(String description) {
        beforePropertyWrite("description", this.description, description);
        this.description = description;
    }

    public String getDescription() {
        beforePropertyRead("description");
        return this.description;
    }

    public void setSequence(Short sequence) {
        beforePropertyWrite("sequence", this.sequence, sequence);
        this.sequence = sequence;
    }

    public Short getSequence() {
        beforePropertyRead("sequence");
        return this.sequence;
    }

    public void setTaskPriorityUuid(String taskPriorityUuid) {
        beforePropertyWrite("taskPriorityUuid", this.taskPriorityUuid, taskPriorityUuid);
        this.taskPriorityUuid = taskPriorityUuid;
    }

    public String getTaskPriorityUuid() {
        beforePropertyRead("taskPriorityUuid");
        return this.taskPriorityUuid;
    }

    public void addToTasks(Task obj) {
        addToManyTarget("tasks", obj, true);
    }

    public void removeFromTasks(Task obj) {
        removeToManyTarget("tasks", obj, true);
    }

    @SuppressWarnings("unchecked")
    public List<Task> getTasks() {
        return (List<Task>)readProperty("tasks");
    }

    @Override
    public Object readPropertyDirectly(String propName) {
        if(propName == null) {
            throw new IllegalArgumentException();
        }

        switch(propName) {
            case "description":
                return this.description;
            case "sequence":
                return this.sequence;
            case "taskPriorityUuid":
                return this.taskPriorityUuid;
            case "tasks":
                return this.tasks;
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
            case "description":
                this.description = (String)val;
                break;
            case "sequence":
                this.sequence = (Short)val;
                break;
            case "taskPriorityUuid":
                this.taskPriorityUuid = (String)val;
                break;
            case "tasks":
                this.tasks = val;
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
        out.writeObject(this.description);
        out.writeObject(this.sequence);
        out.writeObject(this.taskPriorityUuid);
        out.writeObject(this.tasks);
    }

    @Override
    protected void readState(ObjectInputStream in) throws IOException, ClassNotFoundException {
        super.readState(in);
        this.description = (String)in.readObject();
        this.sequence = (Short)in.readObject();
        this.taskPriorityUuid = (String)in.readObject();
        this.tasks = in.readObject();
    }

}
