package pers.dog.app.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import pers.dog.api.dto.HttpProxy;
import pers.dog.app.service.HttpProxyService;

/**
 * @author 废柴 2023/9/21 15:35
 */
@Service
public class HttpProxyServiceImpl implements HttpProxyService {

    @Override
    public void setProxy(HttpProxy proxy) {
        if (proxy.isNoProxy()) {
            System.clearProperty("http.proxyHost");
            System.clearProperty("http.proxyPort");
            System.clearProperty("http.nonProxyHosts");
            System.clearProperty("socksProxyHost");
            System.clearProperty("socksProxyPort");
            System.clearProperty("socksNonProxyHosts");
        } else if (proxy.isManualProxy()) {
            if (proxy.isManualProxyHttp()) {
                if (!ObjectUtils.isEmpty(proxy.getHostName())) {
                    System.setProperty("http.proxyHost", proxy.getHostName());
                }
                if (!ObjectUtils.isEmpty(proxy.getPortNumber())) {
                    System.setProperty("http.proxyPort", String.valueOf(proxy.getPortNumber()));
                }
                if (!ObjectUtils.isEmpty(proxy.getNoProxyHostName())) {
                    System.setProperty("http.nonProxyHosts", proxy.getNoProxyHostName());
                }
            } else if (proxy.isManualProxySocks()) {
                if (!ObjectUtils.isEmpty(proxy.getHostName())) {
                    System.setProperty("socksProxyHost", proxy.getHostName());
                }
                if (!ObjectUtils.isEmpty(proxy.getPortNumber())) {
                    System.setProperty("socksProxyPort", String.valueOf(proxy.getPortNumber()));
                }
                if (!ObjectUtils.isEmpty(proxy.getNoProxyHostName())) {
                    System.setProperty("socksNonProxyHosts", proxy.getNoProxyHostName());
                }
            }
        }
    }
}

