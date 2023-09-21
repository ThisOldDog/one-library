package pers.dog.api.dto;

import pers.dog.infra.constant.HttpProxyType;

/**
 * @author 废柴 2023/9/21 10:06
 */
public class HttpProxy {
    private boolean noProxy;
    // Manual
    private boolean manualProxy;
    private boolean manualProxyHttp;
    private boolean manualProxySocks;
    private String hostName;
    private Integer portNumber;
    private String noProxyHostName;

    public boolean isNoProxy() {
        return noProxy;
    }

    public HttpProxy setNoProxy(boolean noProxy) {
        this.noProxy = noProxy;
        return this;
    }

    public boolean isManualProxy() {
        return manualProxy;
    }

    public HttpProxy setManualProxy(boolean manualProxy) {
        this.manualProxy = manualProxy;
        return this;
    }

    public boolean isManualProxyHttp() {
        return manualProxyHttp;
    }

    public HttpProxy setManualProxyHttp(boolean manualProxyHttp) {
        this.manualProxyHttp = manualProxyHttp;
        return this;
    }

    public boolean isManualProxySocks() {
        return manualProxySocks;
    }

    public HttpProxy setManualProxySocks(boolean manualProxySocks) {
        this.manualProxySocks = manualProxySocks;
        return this;
    }

    public String getHostName() {
        return hostName;
    }

    public HttpProxy setHostName(String hostName) {
        this.hostName = hostName;
        return this;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public HttpProxy setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
        return this;
    }

    public String getNoProxyHostName() {
        return noProxyHostName;
    }

    public HttpProxy setNoProxyHostName(String noProxyHostName) {
        this.noProxyHostName = noProxyHostName;
        return this;
    }
}
