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
package com.hulles.a1icia.hotel.task;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import com.hulles.a1icia.base.A1iciaException;
import com.hulles.a1icia.cayenne.A1iciaApplication;
import com.hulles.a1icia.cayenne.Person;
import com.hulles.a1icia.cayenne.Task;
import com.hulles.a1icia.cayenne.TaskPriority;
import com.hulles.a1icia.cayenne.TaskStatus;
import com.hulles.a1icia.cayenne.TaskType;
import com.hulles.a1icia.tools.A1iciaUtils;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VTodo;
import biweekly.property.Completed;
import biweekly.property.Created;
import biweekly.property.DateDue;
import biweekly.property.Status;
import biweekly.property.Uid;

public class LoadTasks {
	
	public static void loadTasks() {
		String fileName;
		ICalendar ical;
		List<VTodo> todos;
		Task task;
		Completed completed;
		Date date;
		LocalDateTime ldt;
		Created created;
		DateDue dateDue;
		TaskStatus taskStatus;
		Person person;
		TaskPriority taskPriority;
		TaskType taskType;
		
		person = Person.findPerson("hulles");
		taskPriority = TaskPriority.findTaskPriority(2);
		taskType = TaskType.findTaskType(1);
		fileName = "/home/hulles/A1icia Exec/Home.ics";
		A1iciaApplication.setErrorOnUncommittedObjects(false);
		try (Reader reader = new FileReader(fileName)) {
			ical = Biweekly.parse(reader).first();
			System.out.println("ToDo's:");
			todos = ical.getTodos();
			for (VTodo vt : todos) {
				System.out.println("To Do: " + vt.getSummary().getValue());
				task = Task.createNew();
				task.setDescription(vt.getSummary().getValue());
				task.setPerson(person);
				task.setTaskPriority(taskPriority);
				task.setTaskType(taskType);
				if (vt.getCompleted() != null) {
					System.out.print("Completed Date: ");
					System.out.print(vt.getCompleted());
					completed = vt.getCompleted();
					System.out.println("(Date)" + completed.getValue());
					date = completed.getValue();
					ldt = A1iciaUtils.ldtFromUtilDate(date);
					task.setDateCompleted(ldt);
				}
				if (vt.getCreated() != null) {
					System.out.print("Created Date: ");
					System.out.print(vt.getCreated());
					created = vt.getCreated();
					System.out.println("(Date)" + created.getValue());
					date = created.getValue();
					ldt = A1iciaUtils.ldtFromUtilDate(date);
					task.setDateCreated(ldt);
				}
				if (vt.getDateDue() != null) {
					System.out.print("Date Due: ");
					System.out.print(vt.getDateDue());
					dateDue = vt.getDateDue();
					System.out.println("(Date)" + dateDue.getValue());
					date = dateDue.getValue();
					ldt = A1iciaUtils.ldtFromUtilDate(date);
					task.setDateDue(ldt);
				}
				if (vt.getStatus() != null) {
					System.out.print("Status: ");
					System.out.print(vt.getStatus());
					Status status = vt.getStatus();
					System.out.println("(Status)" + status.getValue());
					if (status.getValue().equals("COMPLETED")) {
						taskStatus = TaskStatus.findTaskStatus(2);
						task.setTaskStatus(taskStatus);
					} else if (status.getValue().equals("IN-PROCESS")) {
						taskStatus = TaskStatus.findTaskStatus(1);
						task.setTaskStatus(taskStatus);
					} else {
						throw new A1iciaException();
					}
				}
				if (vt.getUid() != null) {
					System.out.print("Uid: ");
					System.out.print(vt.getUid());
					Uid uuid = vt.getUid();
					System.out.println("(UUID)" + uuid.getValue());
					task.setTaskUuid(uuid.getValue());
				}
				System.out.println();
				task.commit();
			}

//			System.out.println("Events:");
//			events = ical.getEvents();
//			for (VEvent event : events) {
//				summary = event.getSummary().getValue();
//				System.out.println("Event: " + summary);
//			}
		} catch (FileNotFoundException e) {
			throw new A1iciaException("HotelNeo: can't find calendar file", e);
		} catch (IOException e1) {
			throw new A1iciaException("HotelNeo: can't close calendar file", e1);
		}
		A1iciaApplication.setErrorOnUncommittedObjects(false);
	}
	
	public static void main(String[] args) {
		
		loadTasks();
	}
}
