package org.opendatakit.services.forms;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.opendatakit.aggregate.odktables.rest.entity.*;

import java.util.ArrayList;

/**
 * Created by user on 13.02.17.
 */

public class RowList {

    /**
     * The entries in the manifest.
     */
    @JsonProperty(required = false)
    private ArrayList<Row> rows;

    /**
     * The dataETag of the table at the START of this request.
     */
    @JsonProperty(required = false)
    private String dataETag;

    /**
     * The deviceId of the device that send a request.
     */
    @JsonProperty(required = false)
    private String deviceId;

    @JsonProperty(required = false)
    private String officeId;
    /**
     * Constructor used by Jackson
     */
    public RowList() {
        this.rows = new ArrayList<Row>();
    }

    /**
     * Constructor used by our Java code
     *
     * @param entries
     */
    public RowList(ArrayList<Row> rows, String dataETag) {
        if ( rows == null ) {
            this.rows = new ArrayList<Row>();
        } else {
            this.rows = rows;
        }
        this.dataETag = dataETag;
    }

    public ArrayList<Row> getRows() {
        return rows;
    }

    public void setRows(ArrayList<Row> rows) {
        this.rows = rows;
    }

    public String getDataETag() {
        return dataETag;
    }

    public void setDataETag(String dataETag) {
        this.dataETag = dataETag;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rows == null) ? 0 : rows.hashCode());
        result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RowList)) {
            return false;
        }
        RowList other = (RowList) obj;
        boolean simpleResult =
                (rows == null ? other.rows == null : (other.rows != null && rows.size() == other.rows.size())) &&
                        (dataETag == null ? other.dataETag == null : (dataETag.equals(other.dataETag)));
        if ( !simpleResult ) {
            return false;
        }

        if ( rows == null ) {
            return true;
        }

        return rows.containsAll(other.rows);
    }
}
