package Objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vendor {

    @SerializedName("mac")
    @Expose
    private String mac;
    @SerializedName("company")
    @Expose
    private String company;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("mac_start")
    @Expose
    private String macStart;
    @SerializedName("mac_end")
    @Expose
    private String macEnd;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("country")
    @Expose
    private String country;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getMacStart() {
        return macStart;
    }

    public void setMacStart(String macStart) {
        this.macStart = macStart;
    }

    public String getMacEnd() {
        return macEnd;
    }

    public void setMacEnd(String macEnd) {
        this.macEnd = macEnd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}