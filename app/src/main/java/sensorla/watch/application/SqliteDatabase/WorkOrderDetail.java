package sensorla.watch.application.SqliteDatabase;

import java.util.Date;

public class WorkOrderDetail {
    public String work_order_detail_id;
    public String work_order_id;
    public String status;
    public String external_id;
    public String environment;
    public String create_datetime;

    public String getWork_order_detail_id() {
        return work_order_detail_id;
    }

    public void setWork_order_detail_id(String work_order_detail_id) {
        this.work_order_detail_id = work_order_detail_id;
    }

    public String getWork_order_id() {
        return work_order_id;
    }

    public void setWork_order_id(String work_order_id) {
        this.work_order_id = work_order_id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExternal_id() {
        return external_id;
    }

    public void setExternal_id(String external_id) {
        this.external_id = external_id;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getCreate_datetime() {
        return create_datetime;
    }

    public void setCreate_datetime(String create_datetime) {
        this.create_datetime = create_datetime;
    }

}
