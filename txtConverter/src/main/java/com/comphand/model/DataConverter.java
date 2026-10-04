package com.comphand.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Plain model object for Product. See User for the note on why this
 * class must be byte-for-byte identical (including serialVersionUID)
 * in both projects. On comphandservice it is also the Hibernate-mapped
 * class -- see src/main/resources/com/comphand/model/Product.hbm.xml.
 */
public class DataConverter implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String kode;
    private String accNumber;
    private double nominal;
    private String tanda;
    private String noref;
    private String nama;

    public DataConverter() {
    }

    public DataConverter(String id, String kode, String accNumber, double nominal, String tanda, String noref, String nama) {
        this.id = id;
        this.kode = kode;
        this.accNumber = accNumber;
        this.nominal = nominal;
        this.tanda = tanda;
        this.noref = noref;
        this.nama = nama;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKode() {
        return kode;
    }

    public void setKode(String kode) {
        this.kode = kode;
    }

    public String getAccNumber() {
        return accNumber;
    }

    public void setAccNumber(String accNumber) {
        this.accNumber = accNumber;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public String getTanda() {
        return tanda;
    }

    public void setTanda(String tanda) {
        this.tanda = tanda;
    }

    public String getNoref() {
        return noref;
    }

    public void setNoref(String noref) {
        this.noref = noref;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    
}
