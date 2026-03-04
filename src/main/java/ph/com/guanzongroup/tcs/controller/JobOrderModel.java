package ph.com.guanzongroup.tcs.controller;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Maynard
 */
public class JobOrderModel {

    public String getIndex01() {
        return index01.get();
    }

    public void setIndex01(SimpleStringProperty index01) {
        this.index01 = index01;
    }

    public String getIndex02() {
        return index02.get();
    }

    public void setIndex02(SimpleStringProperty index02) {
        this.index02 = index02;
    }

    public String getIndex03() {
        return index03.get();
    }

    public void setIndex03(SimpleStringProperty index03) {
        this.index03 = index03;
    }

    public String getIndex04() {
        return index04.get();
    }

    public void setIndex04(SimpleStringProperty index04) {
        this.index04 = index04;
    }

    public String getIndex05() {
        return index05.get();
    }

    public void setIndex05(SimpleStringProperty index05) {
        this.index05 = index05;
    }

    public String getIndex06() {
        return index06.get();
    }

    public void setIndex06(SimpleStringProperty index06) {
        this.index06 = index06;
    }

    public String getIndex07() {
        return index07.get();
    }

    public void setIndex07(SimpleStringProperty index07) {
        this.index07 = index07;
    }

    public String getIndex08() {
        return index08.get();
    }

    public void setIndex08(SimpleStringProperty index08) {
        this.index08 = index08;
    }

    public String getIndex09() {
        return index09.get();
    }

    public void setIndex09(SimpleStringProperty index09) {
        this.index09 = index09;
    }

    public String getIndex10() {
        return index10.get();
    }

    public void setIndex10(SimpleStringProperty index10) {
        this.index10 = index10;
    }

    public String getIndex11() {
        return index11.get();
    }

    public void setIndex11(SimpleStringProperty index11) {
        this.index11 = index11;
    }

    public void setIndex11(String index11) {

        SimpleStringProperty prop = new SimpleStringProperty(index11);
        this.index11 = prop;
    }

    public String getIndex12() {
        return index12.get();
    }

    public void setIndex12(SimpleStringProperty index12) {
        this.index12 = index12;
    }

    public JobOrderModel(String Index01, String Index02, String Index03, String Index04, String Index05,
            String Index06, String Index07, String Index08, String Index09,
            String Index10, String Index11, String Index12) {

        this.index01 = new SimpleStringProperty(Index01);
        this.index02 = new SimpleStringProperty(Index02);
        this.index03 = new SimpleStringProperty(Index03);
        this.index04 = new SimpleStringProperty(Index04);
        this.index05 = new SimpleStringProperty(Index05);
        this.index06 = new SimpleStringProperty(Index06);
        this.index07 = new SimpleStringProperty(Index07);
        this.index08 = new SimpleStringProperty(Index08);
        this.index09 = new SimpleStringProperty(Index09);
        this.index10 = new SimpleStringProperty(Index10);
        this.index11 = new SimpleStringProperty(Index11);
        this.index12 = new SimpleStringProperty(Index12);

    }
    private SimpleStringProperty index01;
    private SimpleStringProperty index02;
    private SimpleStringProperty index03;
    private SimpleStringProperty index04;
    private SimpleStringProperty index05;
    private SimpleStringProperty index06;
    private SimpleStringProperty index07;
    private SimpleStringProperty index08;
    private SimpleStringProperty index09;
    private SimpleStringProperty index10;
    private SimpleStringProperty index11;
    private SimpleStringProperty index12;
}
