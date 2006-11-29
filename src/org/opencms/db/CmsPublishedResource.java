/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsPublishedResource.java,v $
 * Date   : $Date: 2006/11/29 15:04:13 $
 * Version: $Revision: 1.31.4.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.file.CmsResource;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * Represents the state of a published resource *before* it got published.<p>
 * 
 * This allows various subsequent tasks in the Cms app. (e.g. exporting files and folders)
 * to identify published resources after a resource or project was published.<p>
 * 
 * The values to fill this container are read from the Cms publish history database table 
 * that is written during each publishing process.<p>
 * 
 * @author Thomas Weckert 
 * 
 * @version $Revision: 1.31.4.4 $
 * 
 * @since 6.0.0
 * 
 * @see org.opencms.db.I_CmsProjectDriver#readPublishedResources(CmsDbContext, int, CmsUUID)
 */
public class CmsPublishedResource implements Serializable, Comparable {

    /**
     * Add new resource states under consideration of the move operation.<p>
     */
    public static class CmsPublishedResourceState extends CmsResourceState {

        private static final long serialVersionUID = -2901049208546972463L;

        /**
         * protected constructor.<p>
         * 
         * @param state an integer representing the state 
         * @param abbrev an abbreviation character
         */
        protected CmsPublishedResourceState(int state, char abbrev) {

            super(state, abbrev);
        }

        /**
         * Returns the corresponding resource state for this publish resource state.<p>
         * 
         * @return the corresponding resource state
         */
        public CmsResourceState getResourceState() {

            if (this == STATE_MOVED_SOURCE) {
                return CmsResource.STATE_DELETED;
            } else if (this == STATE_MOVED_DESTINATION) {
                return CmsResource.STATE_NEW;
            } else {
                return null;
            }
        }
    }

    /** Additional state for moved resources, the (new) destination of the moved resource. */
    public static final CmsPublishedResourceState STATE_MOVED_DESTINATION = new CmsPublishedResourceState(12, 'M');

    /** Additional state for moved resources, the (deleted) source of the moved resource. */
    public static final CmsPublishedResourceState STATE_MOVED_SOURCE = new CmsPublishedResourceState(11, ' ');

    /** Serial version UID required for safe serialization. */
    private static final long serialVersionUID = -1054065812825770479L;

    /** The backup tag ID of the published resource. */
    private int m_backupTagId;

    /** Indicates if the published resource is a folder or a file. */
    private boolean m_isFolder;

    /** falg to signal if the resource was moved. */
    private boolean m_isMoved;

    /** The resource ID of the published resource.<p> */
    private CmsUUID m_resourceId;

    /** The state of the resource *before* it was published.<p> */
    private CmsResourceState m_resourceState;

    /** The type of the published resource.<p> */
    private int m_resourceType;

    /** The root path of the published resource.<p> */
    private String m_rootPath;

    /** The count of siblings of the published resource. */
    private int m_siblingCount;

    /** The structure ID of the published resource.<p> */
    private CmsUUID m_structureId;

    /**
     * Creates an object for published VFS resources.<p>
     * 
     * Do not write objects created with this constructor to db, since the backup tag id is not set.<p>
     * 
     * @param resource an CmsResource object to create a CmsPublishedResource from
     */
    public CmsPublishedResource(CmsResource resource) {

        this(resource, -1);
    }

    /**
     * Creates an object for published VFS resources.<p>
     * 
     * @param resource an CmsResource object to create a CmsPublishedResource from
     * @param backupTagId the backup tag id
     */
    public CmsPublishedResource(CmsResource resource, int backupTagId) {

        this(resource, backupTagId, resource.getState());
    }

    /**
     * Creates an object for published VFS resources.<p>
     * 
     * @param resource an CmsResource object to create a CmsPublishedResource from
     * @param state the resource state
     * @param backupTagId the backup tag id
     */
    public CmsPublishedResource(CmsResource resource, int backupTagId, CmsResourceState state) {

        this(
            resource.getStructureId(),
            resource.getResourceId(),
            backupTagId,
            resource.getRootPath(),
            resource.getTypeId(),
            resource.isFolder(),
            state,
            resource.getSiblingCount());
    }

    /**
     * Creates an object for published VFS resources.<p>
     * 
     * @param structureId the structure ID of the published resource
     * @param resourceId the resource ID of the published resource
     * @param backupTagId the resource's tag ID in the backup tables
     * @param rootPath the root path of the published resource
     * @param resourceType the type of the published resource
     * @param isFolder indicates if the published resource is a folder or a file
     * @param resourceState the state of the resource *before* it was published
     * @param siblingCount count of siblings of the published resource
     */
    public CmsPublishedResource(
        CmsUUID structureId,
        CmsUUID resourceId,
        int backupTagId,
        String rootPath,
        int resourceType,
        boolean isFolder,
        CmsResourceState resourceState,
        int siblingCount) {

        m_structureId = structureId;
        m_resourceId = resourceId;
        m_backupTagId = backupTagId;
        m_rootPath = rootPath;
        m_resourceType = resourceType;
        m_isFolder = isFolder;
        m_resourceState = resourceState;
        m_siblingCount = siblingCount;
        if (m_resourceState instanceof CmsPublishedResourceState) {
            m_resourceState = ((CmsPublishedResourceState)m_resourceState).getResourceState();
            m_isMoved = true;
        }
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {

        if (obj == this) {
            return 0;
        }
        if (obj instanceof CmsPublishedResource) {
            if (m_rootPath != null) {
                return m_rootPath.compareTo(((CmsPublishedResource)obj).m_rootPath);
            }
        }
        return 0;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsPublishedResource) {
            if (m_structureId.isNullUUID()) {
                return ((CmsPublishedResource)obj).m_resourceId.equals(m_resourceId);
            } else {
                return ((CmsPublishedResource)obj).m_structureId.equals(m_structureId);
            }
        }
        return false;
    }

    /**
     * Returns the backup tag ID of the published resource.<p>
     * 
     * @return the backup tag ID of the published resource
     */
    public int getBackupTagId() {

        return m_backupTagId;
    }

    /**
     * Returns the resource state including move operation information.<p>
     * 
     * @return the resource state including move operation information
     */
    public CmsResourceState getMovedState() {

        if (!m_isMoved) {
            return getState();
        } else if (getState().isDeleted()) {
            return STATE_MOVED_SOURCE;
        } else if (getState().isNew()) {
            return STATE_MOVED_DESTINATION;
        } else {
            // should never happen
            return getState();
        }
    }

    /**
     * Returns the resource ID of the published resource.<p>
     * 
     * @return the resource ID of the published resource
     */
    public CmsUUID getResourceId() {

        return m_resourceId;
    }

    /**
     * Returns the root path of the published resource.<p>
     * 
     * @return the root path of the published resource
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Returns the count of siblings of the published resource.<p>
     * 
     * If a resource has no sibling, the total sibling count for this resource is <code>1</code>, 
     * if a resource has <code>n</code> siblings, the sibling count is <code>n + 1</code>.<p> 
     * 
     * @return the count of siblings of the published resource
     */
    public int getSiblingCount() {

        return m_siblingCount;
    }

    /**
     * Returns the resource state of the published resource.<p>
     * 
     * @return the resource state of the published resource
     */
    public CmsResourceState getState() {

        return m_resourceState;
    }

    /**
     * Returns the structure ID of the published resource.<p>
     * 
     * @return the structure ID of the published resource
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Returns the resource type of the published resource.<p>
     * 
     * @return the resource type of the published resource
     */
    public int getType() {

        return m_resourceType;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return m_structureId.isNullUUID() ? m_resourceId.hashCode() : m_structureId.hashCode();
    }

    /**
     * Determines if this resource is a file.<p>
     * 
     * @return true if this resource is a file, false otherwise
     */
    public boolean isFile() {

        return !m_isFolder;
    }

    /**
     * Checks if this resource is a folder.<p>
     * 
     * @return true if this is is a folder
     */
    public boolean isFolder() {

        return m_isFolder;
    }

    /**
     * Returns <code>true</code> if the resource has been moved.<p>
     *
     * @return <code>true</code> if the resource has been moved
     */
    public boolean isMoved() {

        return m_isMoved;
    }

    /**
     * Checks if this published resource represents a VFS resource.<p>
     * 
     * If the published resource has no structure id, it is considered to be 
     * no VFS resource.<p>
     * 
     * @return true if this published resource is a VFS resource
     */
    public boolean isVfsResource() {

        return !getStructureId().equals(CmsUUID.getNullUUID());
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer(128);

        result.append("[");
        result.append(this.getClass().getName());
        result.append(": root path: ");
        result.append(m_rootPath);
        result.append(", structure ID: ");
        result.append(m_structureId);
        result.append(", resource ID: ");
        result.append(m_resourceId);
        result.append(", backup tag ID: ");
        result.append(m_backupTagId);
        result.append(", siblings: ");
        result.append(m_siblingCount);
        result.append(", state: ");
        result.append(m_resourceState);
        result.append(", type: ");
        result.append(m_resourceType);
        result.append("]");

        return result.toString();
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            m_structureId = null;
            m_resourceId = null;
            m_rootPath = null;
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }
}