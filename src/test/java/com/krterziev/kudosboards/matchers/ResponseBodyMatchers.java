package com.krterziev.kudosboards.matchers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.test.web.servlet.ResultMatcher;

public class ResponseBodyMatchers {
  private final ObjectMapper objectMapper = new ObjectMapper();

  public <T> ResultMatcher containsObjectAsJson(
      final Object expectedObject,
      final Class<T> targetClass) {
    return mvcResult -> {
      final String json = mvcResult.getResponse().getContentAsString();
      final T actualObject = objectMapper.readValue(json, targetClass);
      assertThat(actualObject).isEqualToComparingFieldByField(expectedObject);
    };
  }

  public <T> ResultMatcher containsObjectsAsJson(
      final List<Object> expectedObjects,
      final Class<T> targetClass) {
    return mvcResult -> {
      final String json = mvcResult.getResponse().getContentAsString();
      final List<T> actualObjects = objectMapper.readValue(json, objectMapper.getTypeFactory()
          .constructCollectionType(List.class, targetClass));
      if(expectedObjects.size() != actualObjects.size()) {
        fail("Mismatched sizes between the lists of expected and actual objects");
      }
      for(int index=0; index<expectedObjects.size(); index++) {
        final T actualObject = actualObjects.get(index);
        final Object expectedObject = expectedObjects.get(index);
        assertThat(actualObject).isEqualToComparingFieldByField(expectedObject);
      }

    };
  }

  public static ResponseBodyMatchers responseBody(){
    return new ResponseBodyMatchers();
  }

}
