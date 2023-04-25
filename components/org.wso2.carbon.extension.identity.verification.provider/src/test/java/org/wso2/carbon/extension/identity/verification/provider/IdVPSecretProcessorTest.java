package org.wso2.carbon.extension.identity.verification.provider;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.SecretResolveManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.ResolvedSecret;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import org.testng.Assert;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

@PrepareForTest({SecretManagerImpl.class, SecretResolveManagerImpl.class})
public class IdVPSecretProcessorTest {

    private IdVPSecretProcessor idVPSecretProcessor;
    private SecretManagerImpl secretManager;
    private SecretResolveManagerImpl secretResolveManager;

//    @BeforeMethod
//    public void setUp() throws Exception {

//        secretManager = mock(SecretManagerImpl.class);
//        secretResolveManager = mock(SecretResolveManagerImpl.class);
//        PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
//        PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
//        idVPSecretProcessor = new IdVPSecretProcessor();
//    }

//    @Test
//    public void testDecryptAssociatedSecrets() throws Exception {
//
//        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
//
//        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
//        SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
//        PowerMockito.whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
//        PowerMockito.whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
//        IdVPSecretProcessor idVPSecretProcessor = new IdVPSecretProcessor();
//
//        PowerMockito.doReturn(true).when(secretManager).isSecretExist(anyString(), anyString());
//
//        ResolvedSecret resolvedSecret = new ResolvedSecret();
//        resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
//        PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(anyString(), anyString());
//
//        idVPSecretProcessor.decryptAssociatedSecrets(identityVerificationProvider);
//        Assert.assertEquals(identityVerificationProvider.getIdVConfigProperties()[0].getValue(),
//                "1234-5678-91234-654246");
//    }
//
//    @Test
//    public void testEncryptAssociatedSecrets() throws Exception {
//
//        IdentityVerificationProvider identityVerificationProvider = addTestIdVProvider();
//
//        Secret secret = new Secret();
//        SecretType secretType = new SecretType();
//        secretType.setId("1234");
//
//        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
//        SecretResolveManagerImpl secretResolveManager = mock(SecretResolveManagerImpl.class);
//        whenNew(SecretManagerImpl.class).withNoArguments().thenReturn(secretManager);
//        whenNew(SecretResolveManagerImpl.class).withNoArguments().thenReturn(secretResolveManager);
//        IdVPSecretProcessor idVPSecretProcessor = mock(IdVPSecretProcessor.class);
//
//        when(secretManager.addSecret(anyString(), any(Secret.class))).thenReturn(secret);
//        when(secretManager.getSecretType(anyString())).thenReturn(secretType);
//        when(secretManager.isSecretExist(anyString(), anyString())).thenReturn(false);
//
////        ResolvedSecret resolvedSecret = new ResolvedSecret();
////        resolvedSecret.setResolvedSecretValue("1234-5678-91234-654246");
////        PowerMockito.doReturn(resolvedSecret).when(secretResolveManager).getResolvedSecret(anyString(), anyString());
//
//        idVPSecretProcessor.encryptAssociatedSecrets(identityVerificationProvider);
//        Assert.assertEquals(identityVerificationProvider.getIdVConfigProperties()[0].getValue(),
//                "1234-5678-91234-654246");
//    }

    @Test
    public void testDeleteAssociatedSecrets() {

    }

    private IdVProvider addTestIdVProvider() {

        IdVProvider idVProvider = new IdVProvider();
        idVProvider.setIdVProviderUUID("1c7ce08b-2ebc-4b9e-a107-3b129c019954");
        idVProvider.setIdVProviderName("ONFIDO");
        idVProvider.setIdVProviderDescription("ONFIDO identity verification provider");

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("http://wso2.org/claims/givenname", "firstName");
        claimMappings.put("http://wso2.org/claims/lastname", "lastName");
        idVProvider.setClaimMappings(claimMappings);

        IdVConfigProperty[] idVConfigProperties = new IdVConfigProperty[2];
        IdVConfigProperty idVConfigProperty1 = new IdVConfigProperty();
        idVConfigProperty1.setName("token");
        idVConfigProperty1.setValue("1234-5678-91234-654246");
        idVConfigProperty1.setConfidential(true);
        idVConfigProperties[0] = idVConfigProperty1;

        IdVConfigProperty idVConfigProperty2 = new IdVConfigProperty();
        idVConfigProperty2.setName("apiUrl");
        idVConfigProperty2.setValue("https://api.test.com/v1/");
        idVConfigProperty2.setConfidential(false);
        idVConfigProperties[1] = idVConfigProperty2;

        idVProvider.setIdVConfigProperties(idVConfigProperties);
        return idVProvider;
    }
}