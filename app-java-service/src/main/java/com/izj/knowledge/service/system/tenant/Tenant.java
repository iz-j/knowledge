package com.izj.knowledge.service.system.tenant;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Builder(toBuilder = true)
@ToString
@EqualsAndHashCode
public class Tenant {
    private final String id;
    private final String serviceName;
    private final String adminName;
    private final String email;
    private final String siteUrl;
    private final String hogeTenantUrl;
    private final String hogeClientId;
    private final String hogeClientSecret;

    /**
     * e.g. "ishikawa-denko"
     *
     * @return tenant id
     */
    public String getId() {
        return id;
    }

    /**
     * Mainly used at email notification.<br>
     * e.g. "Ishikawa Electric Group WEB-EDI"
     *
     * @return tenant serviceName
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Mainly used at a caution on email.<br>
     * e.g. "Ishikawa Electric Co. Ltd,"
     *
     * @return adminName
     */
    public String getAdminName() {
        return adminName;
    }

    /**
     * Return email address mainly used by SES.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Return the url of front page assigned per tenant.<br>
     * e.g. "https://ishikawa-denko"
     *
     * @return url of front page
     */
    public String getSiteUrl() {
        return siteUrl;
    }

    /**
     * Return the url of hoge service.
     *
     * @return url
     */
    public String gethogeTenantUrl() {
        return hogeTenantUrl;
    }

    /**
     * Return the clientId using to connect to hoge service (hoge-Procurement, hoge-BizPartner and so on).<br>
     * cf. ClientCredentialsDto in hoge
     *
     * @return clientId
     */
    public String gethogeClientId() {
        return hogeClientId;
    }

    /**
     * @see #gethogeClientId()
     * @return clientSecret
     */
    public String gethogeClientSecret() {
        return hogeClientSecret;
    }
}
