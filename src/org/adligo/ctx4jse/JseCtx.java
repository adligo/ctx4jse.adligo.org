package org.adligo.ctx4jse;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.adligo.ctx.shared.Ctx;
import org.adligo.ctx.shared.CtxMutant;
import org.adligo.i.ctx4jse.shared.I_PrintCtx;


/**
 * This class a JSE implementation of the Context Creation pattern through
 * either a Functional map of Strings, to {@link Supplier}s, implemented here
 * {@link Ctx} or Reflection using bean (aka zero argument constructors). <br/><br/>
 * 
 * @author scott<br/>
 *         <br/>
 * 
 *         <pre>
 *         <code>
 * ---------------- Apache ICENSE-2.0 --------------------------
 *
 * Copyright 2022 Adligo Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </code>
 * 
 *         <pre>
 */

public class JseCtx implements I_PrintCtx {
  public static final String UNABLE_TO_FIND_S_IN_THIS_CONTEXT = "Unable to find '%s' in this context!";
  public static final String BAD_NAME = "Names passed to the create bean method MUST be java.lang.Class names!\n\t%s";
  public static final String UNABLE_TO_FIND_BEAN_CONSTRUCTOR_FOR_S = "Unable to find bean constructor for %s!";
  public static final Class<?> [] EMPTY_CLAZZ_ARRAY = new Class<?>[] {};
  public static final Object[] EMPTY_OBJECT_ARRAY = new Object[] {};
  public static final String UNABLE_TO_CREATE_INSTANCE_OF_S = "Unable to create instance of %s";
  
  private final Map<String, Object> instanceMap;
  private final Optional<Ctx> ctxOpt;
  
  public JseCtx() {
    this(() -> Optional.empty(), () -> new ConcurrentHashMap<>());
  }
  
  @SuppressWarnings("rawtypes")
  public JseCtx(CtxMutant ctx) {
    this(() -> Optional.of(new Ctx(ctx)),
        () -> new ConcurrentHashMap());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  JseCtx(Supplier<Optional<Ctx>> ctxCreator, Supplier<ConcurrentHashMap> concurrentMapCtxCreation) {
    ctxOpt = ctxCreator.get();
    instanceMap = concurrentMapCtxCreation.get();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T create(Class<T> clazz) {
    if (ctxOpt.isPresent()) {
      Object r = ctxOpt.get().create(clazz);  
      if (r != null) {
        return (T) r;
      }
    }
    Constructor<?> c = null;
    try {
      c = clazz.getConstructor(EMPTY_CLAZZ_ARRAY);
    } catch (NoSuchMethodException | SecurityException e) {
      throw new IllegalStateException(String.format(UNABLE_TO_FIND_BEAN_CONSTRUCTOR_FOR_S, clazz), e);
    }
    
    try {
      return (T) c.newInstance(EMPTY_OBJECT_ARRAY);
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new IllegalStateException(String.format(UNABLE_TO_CREATE_INSTANCE_OF_S, clazz), e);
    }
  }

  @Override
  public Object create(String name) {
    Object r = null;
    if (ctxOpt.isPresent()) {
      r =  ctxOpt.get().create(name);  
      if (r != null) {
        return r;
      }
    }
    try {
      return create(Class.forName(name));
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format(
          BAD_NAME, name));
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Class<T> clazz) {
    if (ctxOpt.isPresent()) {
      Object r = ctxOpt.get().get(clazz);  
      if (r != null) {
        return (T) r;
      }
    }
    String clazzName = clazz.getName();
    Object r = instanceMap.get(clazzName);
    if (r == null) {
      synchronized (instanceMap) {
        r = instanceMap.get(clazzName);
        if (r == null) {
          r = create(clazz);
          instanceMap.put(clazzName, r);
        }
      }
    }
    if (r != null ) {
      return (T) r;
    }
    throw new IllegalStateException(String.format(UNABLE_TO_FIND_S_IN_THIS_CONTEXT, clazzName));
  }
  
  @Override
  public Object get(String name) {
    Object r = null;
    if (ctxOpt.isPresent()) {
      r =  ctxOpt.get().get(name);  
      if (r != null) {
        return r;
      }
    }
    try {
      return get(Class.forName(name));
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format(
          BAD_NAME, name));
    }
  }
  
}
