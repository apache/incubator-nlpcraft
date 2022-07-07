// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/makro/antlr4/NCMacroDsl.g4 by ANTLR 4.10.1
package org.apache.nlpcraft.internal.makro.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCMacroDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LCURLY=1, RCURLY=2, VERT=3, COMMA=4, UNDERSCORE=5, LBR=6, RBR=7, QUESTION=8, 
		REGEX_TXT=9, IDL_TXT=10, TXT=11, MINMAX=12, WS=13, ERR_CHAR=14;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LCURLY", "RCURLY", "VERT", "COMMA", "UNDERSCORE", "LBR", "RBR", "QUESTION", 
			"ESC_CHAR", "ESC", "TXT_CHAR", "REGEX_TXT", "IDL_TXT", "TXT", "MINMAX", 
			"WS", "ERR_CHAR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'|'", "','", "'_'", "'['", "']'", "'?'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LCURLY", "RCURLY", "VERT", "COMMA", "UNDERSCORE", "LBR", "RBR", 
			"QUESTION", "REGEX_TXT", "IDL_TXT", "TXT", "MINMAX", "WS", "ERR_CHAR"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public NCMacroDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCMacroDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\u0004\u0000\u000ej\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002\u0001"+
		"\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004"+
		"\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007"+
		"\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b"+
		"\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002"+
		"\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0001\u0000\u0001\u0000\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001"+
		"\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\b\u0001\b\u0001\t\u0001\t\u0001\t\u0001\n\u0003"+
		"\n:\b\n\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0005\u000b@\b"+
		"\u000b\n\u000b\f\u000bC\t\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001"+
		"\f\u0001\f\u0001\f\u0001\f\u0005\fL\b\f\n\f\f\fO\t\f\u0001\f\u0001\f\u0001"+
		"\f\u0001\r\u0001\r\u0004\rV\b\r\u000b\r\f\rW\u0001\u000e\u0001\u000e\u0004"+
		"\u000e\\\b\u000e\u000b\u000e\f\u000e]\u0001\u000e\u0001\u000e\u0001\u000f"+
		"\u0004\u000fc\b\u000f\u000b\u000f\f\u000fd\u0001\u000f\u0001\u000f\u0001"+
		"\u0010\u0001\u0010\u0002AM\u0000\u0011\u0001\u0001\u0003\u0002\u0005\u0003"+
		"\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f\b\u0011\u0000\u0013\u0000"+
		"\u0015\u0000\u0017\t\u0019\n\u001b\u000b\u001d\f\u001f\r!\u000e\u0001"+
		"\u0000\u0004\u0005\u0000,,//[]__{}\u0011\u0000!Z\\\\^z~~\u00a0\u024f\u0259"+
		"\u0292\u02b0\u036f\u0400\u04ff\u1e02\u1ef3\u1f01\u1fff\u200c\u200d\u203f"+
		"\u2040\u2070\u218f\u2c00\u2fef\u3001\u8000\ud7ff\u8000\uf900\u8000\ufdcf"+
		"\u8000\ufdf0\u8000\ufffd\u0003\u0000  ,,09\u0003\u0000\t\n\f\r  l\u0000"+
		"\u0001\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000"+
		"\u0005\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000"+
		"\t\u0001\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r"+
		"\u0001\u0000\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0017"+
		"\u0001\u0000\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b"+
		"\u0001\u0000\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f"+
		"\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000\u0000\u0001#\u0001\u0000"+
		"\u0000\u0000\u0003%\u0001\u0000\u0000\u0000\u0005\'\u0001\u0000\u0000"+
		"\u0000\u0007)\u0001\u0000\u0000\u0000\t+\u0001\u0000\u0000\u0000\u000b"+
		"-\u0001\u0000\u0000\u0000\r/\u0001\u0000\u0000\u0000\u000f1\u0001\u0000"+
		"\u0000\u0000\u00113\u0001\u0000\u0000\u0000\u00135\u0001\u0000\u0000\u0000"+
		"\u00159\u0001\u0000\u0000\u0000\u0017;\u0001\u0000\u0000\u0000\u0019G"+
		"\u0001\u0000\u0000\u0000\u001bU\u0001\u0000\u0000\u0000\u001dY\u0001\u0000"+
		"\u0000\u0000\u001fb\u0001\u0000\u0000\u0000!h\u0001\u0000\u0000\u0000"+
		"#$\u0005{\u0000\u0000$\u0002\u0001\u0000\u0000\u0000%&\u0005}\u0000\u0000"+
		"&\u0004\u0001\u0000\u0000\u0000\'(\u0005|\u0000\u0000(\u0006\u0001\u0000"+
		"\u0000\u0000)*\u0005,\u0000\u0000*\b\u0001\u0000\u0000\u0000+,\u0005_"+
		"\u0000\u0000,\n\u0001\u0000\u0000\u0000-.\u0005[\u0000\u0000.\f\u0001"+
		"\u0000\u0000\u0000/0\u0005]\u0000\u00000\u000e\u0001\u0000\u0000\u0000"+
		"12\u0005?\u0000\u00002\u0010\u0001\u0000\u0000\u000034\u0007\u0000\u0000"+
		"\u00004\u0012\u0001\u0000\u0000\u000056\u0005\\\u0000\u000067\u0003\u0011"+
		"\b\u00007\u0014\u0001\u0000\u0000\u00008:\u0007\u0001\u0000\u000098\u0001"+
		"\u0000\u0000\u0000:\u0016\u0001\u0000\u0000\u0000;<\u0005/\u0000\u0000"+
		"<=\u0005/\u0000\u0000=A\u0001\u0000\u0000\u0000>@\t\u0000\u0000\u0000"+
		"?>\u0001\u0000\u0000\u0000@C\u0001\u0000\u0000\u0000AB\u0001\u0000\u0000"+
		"\u0000A?\u0001\u0000\u0000\u0000BD\u0001\u0000\u0000\u0000CA\u0001\u0000"+
		"\u0000\u0000DE\u0005/\u0000\u0000EF\u0005/\u0000\u0000F\u0018\u0001\u0000"+
		"\u0000\u0000GH\u0005^\u0000\u0000HI\u0005^\u0000\u0000IM\u0001\u0000\u0000"+
		"\u0000JL\t\u0000\u0000\u0000KJ\u0001\u0000\u0000\u0000LO\u0001\u0000\u0000"+
		"\u0000MN\u0001\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000NP\u0001\u0000"+
		"\u0000\u0000OM\u0001\u0000\u0000\u0000PQ\u0005^\u0000\u0000QR\u0005^\u0000"+
		"\u0000R\u001a\u0001\u0000\u0000\u0000SV\u0003\u0015\n\u0000TV\u0003\u0013"+
		"\t\u0000US\u0001\u0000\u0000\u0000UT\u0001\u0000\u0000\u0000VW\u0001\u0000"+
		"\u0000\u0000WU\u0001\u0000\u0000\u0000WX\u0001\u0000\u0000\u0000X\u001c"+
		"\u0001\u0000\u0000\u0000Y[\u0005[\u0000\u0000Z\\\u0007\u0002\u0000\u0000"+
		"[Z\u0001\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000][\u0001\u0000\u0000"+
		"\u0000]^\u0001\u0000\u0000\u0000^_\u0001\u0000\u0000\u0000_`\u0005]\u0000"+
		"\u0000`\u001e\u0001\u0000\u0000\u0000ac\u0007\u0003\u0000\u0000ba\u0001"+
		"\u0000\u0000\u0000cd\u0001\u0000\u0000\u0000db\u0001\u0000\u0000\u0000"+
		"de\u0001\u0000\u0000\u0000ef\u0001\u0000\u0000\u0000fg\u0006\u000f\u0000"+
		"\u0000g \u0001\u0000\u0000\u0000hi\t\u0000\u0000\u0000i\"\u0001\u0000"+
		"\u0000\u0000\b\u00009AMUW]d\u0001\u0006\u0000\u0000";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}