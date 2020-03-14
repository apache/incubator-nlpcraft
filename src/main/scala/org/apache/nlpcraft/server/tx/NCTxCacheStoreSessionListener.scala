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

package org.apache.nlpcraft.server.tx

import java.sql.SQLException

import javax.cache.integration.CacheWriterException
import org.apache.ignite.cache.store.{CacheStoreSession, CacheStoreSessionListener}

/**
  * Universal cache store session listener.
  */
class NCTxCacheStoreSessionListener extends CacheStoreSessionListener {
    /**
      *
      * @param ses Store session.
      * @param commit Commit/rollback flag.
      */
    override def onSessionEnd(ses: CacheStoreSession, commit: Boolean): Unit = {
        if (ses != null && ses.isWithinTransaction) {
            val con = NCTxManager.connection(ses.transaction())
            
            if (con != null) {
                try                  
                    if (commit)
                        con.commit()
                    else
                        con.rollback()
                catch {
                    case e: SQLException ⇒ throw new CacheWriterException(e)
                }
            }
        }
    }
    
    override def onSessionStart(ses: CacheStoreSession): Unit = {
        // No-op.
    }
}
