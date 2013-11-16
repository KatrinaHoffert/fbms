/*
 * FBMS: File Backup and Management System Copyright (C) 2013 Group 06
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package cmpt370.fbms;


/**
 * Largely a plain old data class for grouping together information on revisions.
 */
public class RevisionInfo implements Comparable<RevisionInfo>
{
	public long id;
	public String path;
	public String diff;
	public long delta;
	public byte[] binary;
	public long filesize;
	public long time;

	@Override
	public int compareTo(RevisionInfo o)
	{
		return (int) (o.time - this.time);
	}
}
