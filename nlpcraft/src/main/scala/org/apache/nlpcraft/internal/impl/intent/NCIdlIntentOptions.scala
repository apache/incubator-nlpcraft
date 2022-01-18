package org.apache.nlpcraft.internal.impl.intent

class NCIdlIntentOptions:
    var ignoreUnusedFreeWords: Boolean = true // Whether to ignore unused free words for intent match.
    var ignoreUnusedSystemTokens: Boolean = true // Whether to ignore unused system tokens for intent match.
    var ignoreUnusedUserTokens: Boolean = false // Whether to ignore unused user tokens for intent match.
    var allowStmTokenOnly: Boolean = false // Whether or not to allow intent to match if all matching tokens came from STM only.
    var ordered: Boolean = false // Whether or not the order of term is important for intent match.

object NCIdlIntentOptions:
    /*
    * JSON field names.
    */
    final val JSON_UNUSED_FREE_WORDS = "unused_free_words"
    final val JSON_UNUSED_SYS_TOKS = "unused_sys_toks"
    final val JSON_UNUSED_USR_TOKS = "unused_usr_toks"
    final val JSON_ALLOW_STM_ONLY = "allow_stm_only"
    final val JSON_ORDERED = "ordered"