package com.comphand.viewmodel;

import java.util.List;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.NotifyChange;
import jxl.Workbook;
import com.comphand.model.DataConverter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import jxl.Sheet;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.VariableResolver;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

@VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class TxtConverterVM {
    private String strFileName, judulTxt, kontenTxt, strSelected, kodeGL;
    private String strFilePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("%20", " ").replace("TxtConverterVM.class", "");
    private int jmlhrecord=0;
    private double sumNominal=0;

    public String getStrFileName() {
        return strFileName;
    }

    public void setStrFileName(String strFileName) {
        this.strFileName = strFileName;
    }

    @NotifyChange("strSelected")
    public String getStrSelected() {
        return strSelected;
    }

    public void setStrSelected(String strSelected) {
        this.strSelected = strSelected;
    }
    
    @Command @NotifyChange("strFileName")
    public void browseFile(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws Exception {
        UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
        if(event.getMedia().getContentType().equals("application/vnd.ms-excel"))
        {
            File file = new File(strFilePath, event.getMedia().getName());
            OutputStream os = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            InputStream is = event.getMedia().getStreamData();
            BufferedInputStream bis = new BufferedInputStream(is);
            byte buffer[] = new byte[1024];
            int ch = bis.read(buffer);
            while (ch!=-1) {
                bos.write(buffer, 0, ch);
                ch=bis.read(buffer);
            }
            bos.close();
            bis.close();
            setStrFileName(event.getMedia().getName());
        }
        else
        {
            Messagebox.show("File yang diupload bukan format excel");
        }
    }
    
    @Command @NotifyChange({"strFileName","lstModShift"})
    public void uploadFile() throws Exception{
        if(getStrFileName()==null || getStrFileName().equals(""))
        {
            Messagebox.show("Pilih File Terlebih Dahulu");
        }
        else{
            if(getStrSelected().equals("0"))
            {
                jmlhrecord=0;
                kontenTxt="";
                sumNominal=0;
                PrintWriter writer = new PrintWriter(new File(strFilePath, "DATAHASIL.txt"), "UTF-8");
                Workbook wb = Workbook.getWorkbook(new File(strFilePath,getStrFileName()));
                Sheet sh = wb.getSheet(0);
                List<DataConverter> lstUpShift = new ArrayList<DataConverter>();
                DataConverter shft = new DataConverter();
                for(int i=1;i<sh.getRows();i++)
                {
                    shft.setKode(sh.getCell(0,i).getContents());
                    shft.setAccNumber(sh.getCell(1,i).getContents());
                    shft.setNominal(Double.parseDouble(sh.getCell(2,i).getContents()));
                    shft.setTanda(sh.getCell(3,i).getContents());
                    shft.setNoref(sh.getCell(4,i).getContents());
                    shft.setNama(sh.getCell(5,i).getContents());
                    lstUpShift.add(shft);
                    jmlhrecord++;
                    sumNominal+=shft.getNominal();
                    kontenTxt+= "1001"+StringUtils.rightPad(shft.getNoref(), 20, " ")+StringUtils.rightPad(shft.getNama().trim().replace("'", ""), 40, " ")
                            +StringUtils.rightPad(" ", 15, " ")+StringUtils.rightPad(shft.getAccNumber(), 19, " ")+StringUtils.rightPad(" ", 291, " ")
                            +"IDR"+StringUtils.leftPad(new DecimalFormat("#.00").format(shft.getNominal()), 21, ""+0)
                            +StringUtils.leftPad(new DecimalFormat("#.00").format(0), 21, ""+0)
                            +StringUtils.rightPad("Pembayaran tagihan bulan", 40, " ")+StringUtils.rightPad(" ", 43, " ")+"#";
                }
                wb.close();
                
                judulTxt="0ABCGROUP";
                judulTxt+=StringUtils.leftPad(String.valueOf(jmlhrecord), 5,""+0);
                judulTxt+=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                judulTxt+=StringUtils.leftPad(new DecimalFormat("#.00").format(sumNominal), 21,""+0);
                judulTxt+=new SimpleDateFormat("yyyyMMdd").format(new Date());
                
                String [] arrDetailKonten = kontenTxt.split("#");
                writer.print(judulTxt);
                writer.println();
                
                for(String i:arrDetailKonten)
                {
                    writer.print(i);
                    writer.println();
                }
                writer.close();
                Filedownload.save(new File(strFilePath+"/DATAHASIL.txt"), null);
            }
            else if(getStrSelected().equals("1"))
            {
                jmlhrecord=0;
                kontenTxt="";
                sumNominal=0;
                
                PrintWriter writer = new PrintWriter(new File(strFilePath, "DATAHASILM2M.txt"), "UTF-8");
                Workbook wb = Workbook.getWorkbook(new File(strFilePath,getStrFileName()));
                Sheet sh = wb.getSheet(0);
                List<DataConverter> lstUpShift = new ArrayList<DataConverter>();
                DataConverter shft = new DataConverter();
                for(int i=1;i<sh.getRows();i++)
                {
                    shft.setKode(sh.getCell(0,i).getContents());
                    shft.setAccNumber(sh.getCell(1,i).getContents());
                    shft.setNominal(Double.parseDouble(sh.getCell(2,i).getContents()));
                    shft.setTanda(sh.getCell(3,i).getContents());
                    shft.setNoref(sh.getCell(4,i).getContents());
                    shft.setNama(sh.getCell(5,i).getContents());
                    lstUpShift.add(shft);
                    jmlhrecord++;
                    sumNominal+=shft.getNominal();
                    
                    if(shft.getKode().contains("AXA"))
                    {
                        kodeGL="1232435005732732780";
                    }
                    else if(shft.getKode().contains("614"))
                    {
                        kodeGL="1335648576876827823";
                    }
                    else if(shft.getKode().contains("1002"))
                    {
                        kodeGL="2452764661241243124";
                    }
                    else if(shft.getKode().contains("QRIS"))
                    {
                        kodeGL="2130070000000015000";
                    }
                    else if(shft.getKode().contains("K4P"))
                    {
                        kodeGL="2130070000050005000";
                    }
                    else if(shft.getKode().contains("5009"))
                    {
                        kodeGL="2130070015000005000";
                    }
                    else if(shft.getKode().contains("PRIM"))
                    {
                        kodeGL="2130070000055005000";
                    }
                    else if(shft.getKode().contains("JALI"))
                    {
                        kodeGL="2130070008780005000";
                    }
                    else if(shft.getKode().contains("2001"))
                    {
                        kodeGL="2130070000000105000";
                    }
                    else if(shft.getKode().contains("MDR1"))
                    {
                        kodeGL="2130045000000005000";
                    }
                    else if(shft.getKode().contains("MDR2"))
                    {
                        kodeGL="8130070000000005000";
                    }
                    else if(shft.getKode().contains("SIRQ"))
                    {
                        kodeGL="8130070004500005000";
                    }
                    else if(shft.getKode().contains("WITH"))
                    {
                        kodeGL="8130070002330000500";
                    }
                    else if(shft.getKode().contains("WAIV"))
                    {
                        kodeGL="2130463721400005000";
                    }
                    else if(shft.getKode().contains("LLD1")||shft.getKode().contains("RTTT"))
                    {
                        kodeGL="4235253257652377280";
                    }
                    else if(shft.getKode().contains("EQUI"))
                    {
                        kodeGL="7438276824234215515";
                    }
                    else if(shft.getKode().contains("LOSS"))
                    {
                        kodeGL="2434247007700707071";
                    }
                    else if(shft.getKode().contains("2RDM"))
                    {
                        kodeGL="8865865221324656200";
                    }
                    else if(shft.getKode().contains("BPCR"))
                    {
                        kodeGL="5342523723572107004";
                    }
                    else if(shft.getKode().contains("PTAB"))
                    {
                        kodeGL="3425324570057587266";
                    }
                    else if(shft.getKode().contains("PGIT"))
                    {
                        kodeGL="4727324734273427805";
                    }
                    
                    if(shft.getTanda().equals("+"))
                    {
                        kontenTxt+= "1A"+StringUtils.rightPad(shft.getAccNumber(), 19," ")+"C"+StringUtils.leftPad(String.format("%.0f", shft.getNominal()), 21,""+0)
                                +shft.getNama()+"#";
                        kontenTxt+= "1G"+kodeGL+"D"+StringUtils.leftPad(String.format("%.0f", shft.getNominal()), 21,""+0)
                                +shft.getNama()+"#";
                    }
                    else if(shft.getTanda().equals("-"))
                    {
                        kontenTxt+= "1A"+StringUtils.rightPad(shft.getAccNumber(), 19," ")+"D"+StringUtils.leftPad(String.format("%.0f", shft.getNominal()), 21,""+0)
                                +shft.getNama()+"#";
                        kontenTxt+= "1G"+kodeGL+"C"+StringUtils.leftPad(String.format("%.0f", shft.getNominal()), 21,""+0)
                                +shft.getNama()+"#";
                    }
                }
                wb.close();
                
                judulTxt="0UPLM2MBASMINTA"+StringUtils.rightPad("MANY TO MANY", 40," ")+"IDR";
                judulTxt+=StringUtils.leftPad(String.valueOf(jmlhrecord*2), 5,""+0);
                judulTxt+=new SimpleDateFormat("yyyyMMdd").format(new Date())+StringUtils.rightPad("010", 5," ");
                
                String [] arrDetailKonten = kontenTxt.split("#");
                writer.print(judulTxt);
                writer.println();
                
                for(String i:arrDetailKonten)
                {
                    writer.print(i);
                    writer.println();
                }
                writer.close();
                Filedownload.save(new File(strFilePath+"/DATAHASILM2M.txt"), null);
            }
            
            Messagebox.show("Proses Selesai");
        }
    }
}
