/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.common.opencensus

import io.opencensus.common.Scope
import io.opencensus.trace._
import org.apache.nlpcraft.common._

import scala.util.control.NonFatal

/**
  * OpenCensus traces instrumentation.
  */
trait NCOpenCensusTrace {
    /**
      *
      * @return
      */
    def tracer(): Tracer = Tracing.getTracer
    
    /**
      * 
      * @return
      */
    def currentSpan(): Span = tracer().getCurrentSpan
    
    /**
      *
      * @param span Span to add tags to.
      * @param tags Span tags to add to the given span.
      */
    def addTags(
        span: Span,
        tags: (String, Any)*
    ): Unit = {
        val attrs = new java.util.HashMap[String, AttributeValue]()

        // Mandatory tags.
        attrs.put("thread", AttributeValue.stringAttributeValue(Thread.currentThread().getName))
        attrs.put("timestamp", AttributeValue.longAttributeValue(System.currentTimeMillis()))
    
        for ((k, v) ← tags if v != null) v match {
            case s: String ⇒ attrs.put(k, AttributeValue.stringAttributeValue(s))
            case l: Long ⇒ attrs.put(k, AttributeValue.longAttributeValue(l))
            case b: Boolean ⇒ attrs.put(k, AttributeValue.booleanAttributeValue(b))
            case d: Double ⇒ attrs.put(k, AttributeValue.doubleAttributeValue(d))
            case _ ⇒ attrs.put(k, AttributeValue.stringAttributeValue(if (v == null) "" else v.toString))
        }
        
        span.putAttributes(attrs)
    }
    
    /**
      *
      * @param name Span name. Will be prepended by the runtime class simple name.
      * @return
      */
    private def mkSpanName(name: String): String = U.cleanClassName(getClass) + '.' + name
    
    /**
      *
      * @param name Span name. Will be prepended by the runtime class simple name.
      * @param tags Span tags to add to the new span.
      * @return
      */
    def startSpan(name: String, tags: (String, Any)*): Span = {
        val span = tracer().spanBuilder(mkSpanName(name)).startSpan()
        
        addTags(span, tags: _*)
        
        span
    }
    
    /**
      *
      * @param name Span name. Will be prepended by the runtime class simple name.
      * @param parent An explicit parent span. If `null` - the default thread-local parent detection will be used.
      * @param tags Span tags to add to the new span.
      * @return
      */
    def startSpan(name: String, parent: Span, tags: (String, Any)*): Span = {
        val span = (
            if (parent == null)
                tracer().spanBuilder(mkSpanName(name))
            else
                tracer().spanBuilderWithExplicitParent(mkSpanName(name), parent)
        )
        .startSpan()
        
        addTags(span, tags: _*)
        
        span
    }
    
    /**
      * Starts scoped span without a parent span.
     *
      * @param name Span name. Will be prepended by the runtime class simple name.
      * @param tags Span tags to add to the new span.
      * @param f
      * @tparam V
      * @return
      */
    def startScopedSpan[V](name: String, tags: (String, Any)*)(f: Span ⇒ V): V =
        startScopedSpan[V](name, null, tags:_*)(f)
    
    /**
      * Starts scoped span with an explicit parent span.
      *
      * @param name Span name. Will be prepended by the runtime class simple name.
      * @param parent An explicit parent span. If `null` - the default thread-local parent detection will be used.
      * @param tags Span tags to add to the new span.
      * @param f
      * @tparam V
      * @return
      */
    def startScopedSpan[V](name: String, parent: Span, tags: (String, Any)*)(f: Span ⇒ V): V = {
        val t = tracer()
    
        /**
          *
          * 
          * @param e
          * @param scope
          */
        def closeAndAddSuppressed(e: Throwable, scope: Scope): Unit = {
            def span(): Span = t.getCurrentSpan
    
            if (e != null)
                try {
                    scope.close()
                
                    span().setStatus(Status.OK)
                }
                catch {
                    case NonFatal(suppressed) =>
                        span().setStatus(Status.INTERNAL.withDescription(suppressed.getMessage))
                    
                        e.addSuppressed(suppressed)
                
                    case fatal: Throwable if NonFatal(e) =>
                        span().setStatus(Status.INTERNAL.withDescription(e.getMessage))
                    
                        fatal.addSuppressed(e)
                    
                        throw fatal
                
                    case fatal: InterruptedException =>
                        fatal.addSuppressed(e)
                    
                        throw fatal
                
                    case fatal: Throwable =>
                        span().setStatus(Status.INTERNAL.withDescription(fatal.getMessage))
                    
                        e.addSuppressed(fatal)
                }
            else
                scope.close()
        }
    
        val scope = (
            if (parent == null)
                t.spanBuilder(mkSpanName(name))
            else
                t.spanBuilderWithExplicitParent(mkSpanName(name), parent)
            )
            .startScopedSpan()
    
        var ex: Throwable = null
        
        val span = t.getCurrentSpan
    
        addTags(span, tags: _*)
    
        try
            f(span)
        catch {
            case e: Throwable =>
                ex = e
            
                throw e
        }
        finally
            closeAndAddSuppressed(ex, scope)
    }
}
