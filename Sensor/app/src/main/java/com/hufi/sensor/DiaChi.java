package com.hufi.sensor;

public class DiaChi {
    int id;
    String date;
    String diaChi;

    public DiaChi(String date, String diaChi) {
        this.date = date;
        this.diaChi = diaChi;
    }

    @Override
    public String toString() {
        return "DiaChi{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", diaChi='" + diaChi + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }
}
