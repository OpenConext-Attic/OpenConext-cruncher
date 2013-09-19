package org.surfnet.cruncher.util;

import java.net.URI;
import java.util.List;

import nl.surfnet.coin.janus.Janus;
import nl.surfnet.coin.janus.domain.ARP;
import nl.surfnet.coin.janus.domain.EntityMetadata;
import nl.surfnet.coin.janus.domain.JanusEntity;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.core.io.ClassPathResource;

public class JanusRestClientMock implements Janus {

  @Override
  public EntityMetadata getMetadataByEntityId(String entityId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getEntityIdsByMetaData(Metadata key, String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllowedSps(String idpentityid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getAllowedSps(String idpentityid, String revision) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<EntityMetadata> getSpList() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<EntityMetadata> getIdpList() {
    TypeReference<List<EntityMetadata>> typeReference = new TypeReference<List<EntityMetadata>>() {};
    return (List<EntityMetadata>) parseJsonData(typeReference, "janus-json/idp.json");
  }
  
  private Object parseJsonData(TypeReference<? extends Object> typeReference, String jsonFile) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(new ClassPathResource(jsonFile).getInputStream(), typeReference);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ARP getArp(String entityId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isConnectionAllowed(String spEntityId, String idpEntityId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public JanusEntity getEntity(String entityId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setJanusUri(URI janusUri) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setUser(String user) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setSecret(String secret) {
    // TODO Auto-generated method stub
    
  }

}
