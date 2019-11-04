package sensorla.watch.application.SqliteDatabase;

import java.util.Date;

public class WorkOrder {
        public String work_order_id;
        public String work_order_name;
        public String work_order_location_name;
        public String work_order_instruction;
        public String work_order_priority;
        public String work_order_status;
        public Boolean work_order_snooze;
        public Date snooze_datetime;
        public Date lastStatusTime;
        public String work_order_type;

        public String getWork_order_id() {
            return work_order_id;
        }

        public void setWork_order_id(String work_order_id) {
            this.work_order_id = work_order_id;
        }

        public String getWork_order_name() {
            return work_order_name;
        }

        public void setWork_order_name(String work_order_name) {
            this.work_order_name = work_order_name;
        }

        public String getWork_order_location_name() {
            return work_order_location_name;
        }

        public void setWork_order_location_name(String work_order_location_name) {
            this.work_order_location_name = work_order_location_name;
        }

        public String getWork_order_instruction() {
            return work_order_instruction;
        }

        public void setWork_order_instruction(String work_order_instruction) {
            this.work_order_instruction = work_order_instruction;
        }

        public String getWork_order_priority() {
            return work_order_priority;
        }

        public void setWork_order_priority(String work_order_priority) {
            this.work_order_priority = work_order_priority;
        }

        public String getWork_order_status() {
            return work_order_status;
        }

        public void setWork_order_status(String work_order_status) {
            this.work_order_status = work_order_status;
        }

        public Boolean getWork_order_snooze() {
            return work_order_snooze;
        }

        public void setWork_order_snooze(Boolean work_order_snooze) {
            this.work_order_snooze = work_order_snooze;
        }

        public Date getSnooze_datetime() {
            return snooze_datetime;
        }

        public void setSnooze_datetime(Date snooze_datetime) {
            this.snooze_datetime = snooze_datetime;
        }

        public Date getLastStatusTime() {
            return lastStatusTime;
        }

        public void setLastStatusTime(Date lastStatusTime) {
            this.lastStatusTime = lastStatusTime;
        }

        public String getWork_order_type() {
            return work_order_type;
        }

        public void setWork_order_type(String work_order_type) {
            this.work_order_type = work_order_type;
        }
}