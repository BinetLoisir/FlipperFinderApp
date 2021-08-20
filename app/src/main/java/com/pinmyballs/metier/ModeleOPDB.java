package com.pinmyballs.metier;

public class ModeleOPDB {

        String id;
        String model;
        String manufacturer;
        String year;

        public ModeleOPDB(String id, String model, String manufacturer, String year) {
                this.id = id;
                this.model = model;
                this.manufacturer = manufacturer;
                this.year = year;
        }


        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getModel() {
                return model;
        }

        public void setModel(String model) {
                this.model = model;
        }

        public String getManufacturer() {
                return manufacturer;
        }

        public void setManufacturer(String manufacturer) {
                this.manufacturer = manufacturer;
        }

        public String getYear() {
                return year;
        }

        public void setYear(String year) {
                this.year = year;
        }
}
