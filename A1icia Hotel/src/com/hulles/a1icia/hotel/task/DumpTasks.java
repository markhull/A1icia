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
package com.hulles.a1icia.hotel.task;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import com.hulles.a1icia.base.A1iciaException;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VTodo;
import biweekly.property.Categories;

public class DumpTasks {
	
	public static void getMyCalendar() {
		String fileName;
		ICalendar ical;
		List<VTodo> todos;
		
		fileName = "/home/hulles/Public/Home.ics";
		try (Reader reader = new FileReader(fileName)) {
			ical = Biweekly.parse(reader).first();
			System.out.println("ToDo's:");
			todos = ical.getTodos();
			for (VTodo vt : todos) {
				System.out.println("To Do: " + vt.getSummary().getValue());
				if (vt.getAlarms() != null) {
					System.out.print("Alarms: ");
					if (vt.getAlarms().isEmpty()) {
						System.out.println("empty");
					} else {
						for (VAlarm valarm : vt.getAlarms()) {
							System.out.print(valarm.getTrigger());
							System.out.print(",");
							System.out.print(valarm.getAction());
							System.out.print("; ");
						}
						System.out.println();
					}
				}
				if (vt.getAttachments() != null) {
					System.out.print("Attachments: ");
					if (vt.getAttachments().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getAttendees() != null) {
					System.out.print("Attendees: ");
					if (vt.getAttendees().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getCategories() != null) {
					System.out.print("Categories: ");
					if (vt.getCategories().isEmpty()) {
						System.out.println("empty");
					} else {
						for (Categories cat : vt.getCategories()) {
							System.out.print(cat);
							System.out.print(",");
						}
						System.out.println();
					}
				}
				if (vt.getClassification() != null) System.out.println("Classification");
				if (vt.getColor() != null) System.out.println("Color");
				if (vt.getComments() != null) {
					System.out.print("Comments: ");
					if (vt.getComments().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getCompleted() != null) {
					System.out.print("Completed: ");
					System.out.println(vt.getCompleted());
				}
				if (vt.getConferences() != null) {
					System.out.print("Conferences: ");
					if (vt.getConferences().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getContacts() != null) {
					System.out.print("Contacts: ");
					if (vt.getContacts().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getCreated() != null) {
					System.out.print("Created: ");
					System.out.println(vt.getCreated());
				}
				if (vt.getDateDue() != null) {
					System.out.print("Date Due: ");
					System.out.println(vt.getDateDue());
				}
				if (vt.getDateStart() != null) {
					System.out.print("Date Start: ");
					System.out.println(vt.getDateStart());
				}
				if (vt.getDateTimeStamp() != null) {
					System.out.print("Date Timestamp: ");
					System.out.println(vt.getDateTimeStamp());
				}
				if (vt.getDescription() != null) {
					System.out.print("Description: ");
					System.out.println(vt.getDescription());
				}
				if (vt.getDuration() != null) {
					System.out.print("Duration: ");
					System.out.println(vt.getDuration());
				}
				if (vt.getExceptionDates() != null) {
					System.out.print("ExceptionDates: ");
					if (vt.getExceptionDates().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getExceptionRules() != null) {
					System.out.print("ExceptionRules: ");
					if (vt.getExceptionRules().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getGeo() != null) System.out.println("Geo");
				if (vt.getImages() != null) {
					System.out.print("Images: ");
					if (vt.getImages().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getLastModified() != null) {
					System.out.print("LastMod: ");
					System.out.println(vt.getLastModified());
				}
				if (vt.getLocation() != null) System.out.println("Location");
				if (vt.getOrganizer() != null) System.out.println("Organizer");
				if (vt.getPercentComplete() != null) {
					System.out.print("PctComplete: ");
					System.out.println(vt.getPercentComplete());
				}
				if (vt.getPriority() != null) {
					System.out.print("Priority");
					System.out.println(vt.getPriority());
				}
				if (vt.getRecurrenceDates() != null) {
					System.out.print("RecurrenceDates: ");
					if (vt.getRecurrenceDates().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getRecurrenceRule() != null) System.out.println("RecurrenceRule");
				if (vt.getRequestStatus() != null) System.out.println("RequestStatus");
				if (vt.getResources() != null) {
					System.out.print("Resources: ");
					if (vt.getResources().isEmpty()) {
						System.out.println("empty");
					} else {
						System.out.println("NONempty");
					}
				}
				if (vt.getSequence() != null) {
					System.out.print("Sequence: ");
					System.out.println(vt.getSequence());
				}
				if (vt.getStatus() != null) {
					System.out.print("Status: ");
					System.out.println(vt.getStatus());
				}
				if (vt.getUid() != null) {
					System.out.print("Uid: ");
					System.out.println(vt.getUid());
				}
				System.out.println();
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
	}
	
	public static void main(String[] args) {
		
		getMyCalendar();
	}
}
