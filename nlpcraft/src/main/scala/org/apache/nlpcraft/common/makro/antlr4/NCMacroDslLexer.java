// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/common/makro/antlr4\NCMacroDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.common.makro.antlr4;
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
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LCURLY=1, RCURLY=2, VERT=3, COMMA=4, UNDERSCORE=5, MINMAX=6, REGEX_TXT=7, 
		DSL_TXT=8, TXT=9, WS=10, ERR_CHAR=11;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LCURLY", "RCURLY", "VERT", "COMMA", "UNDERSCORE", "ESC_CHAR", "ESC", 
			"TXT_CHAR", "MINMAX", "REGEX_TXT", "DSL_TXT", "TXT", "WS", "ERR_CHAR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'|'", "','", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LCURLY", "RCURLY", "VERT", "COMMA", "UNDERSCORE", "MINMAX", "REGEX_TXT", 
			"DSL_TXT", "TXT", "WS", "ERR_CHAR"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\r`\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\5\t\60\n\t\3\n\3\n\6\n\64\n\n\r\n\16"+
		"\n\65\3\n\3\n\3\13\3\13\3\13\3\13\7\13>\n\13\f\13\16\13A\13\13\3\13\3"+
		"\13\3\13\3\f\3\f\3\f\3\f\7\fJ\n\f\f\f\16\fM\13\f\3\f\3\f\3\f\3\r\3\r\6"+
		"\rT\n\r\r\r\16\rU\3\16\6\16Y\n\16\r\16\16\16Z\3\16\3\16\3\17\3\17\4?K"+
		"\2\20\3\3\5\4\7\5\t\6\13\7\r\2\17\2\21\2\23\b\25\t\27\n\31\13\33\f\35"+
		"\r\3\2\6\6\2..]_aa}\177\17\2#@B\\^^`|\u0080\u0080\u00a2\u2001\u200e\u200f"+
		"\u2041\u2042\u2072\u2191\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff"+
		"\5\2\"\"..\62;\5\2\13\f\16\17\"\"\2b\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\3\37\3\2\2\2\5!\3\2\2\2\7#\3\2\2\2"+
		"\t%\3\2\2\2\13\'\3\2\2\2\r)\3\2\2\2\17+\3\2\2\2\21/\3\2\2\2\23\61\3\2"+
		"\2\2\259\3\2\2\2\27E\3\2\2\2\31S\3\2\2\2\33X\3\2\2\2\35^\3\2\2\2\37 \7"+
		"}\2\2 \4\3\2\2\2!\"\7\177\2\2\"\6\3\2\2\2#$\7~\2\2$\b\3\2\2\2%&\7.\2\2"+
		"&\n\3\2\2\2\'(\7a\2\2(\f\3\2\2\2)*\t\2\2\2*\16\3\2\2\2+,\7^\2\2,-\5\r"+
		"\7\2-\20\3\2\2\2.\60\t\3\2\2/.\3\2\2\2\60\22\3\2\2\2\61\63\7]\2\2\62\64"+
		"\t\4\2\2\63\62\3\2\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65\66\3\2\2\2\66\67"+
		"\3\2\2\2\678\7_\2\28\24\3\2\2\29:\7\61\2\2:;\7\61\2\2;?\3\2\2\2<>\13\2"+
		"\2\2=<\3\2\2\2>A\3\2\2\2?@\3\2\2\2?=\3\2\2\2@B\3\2\2\2A?\3\2\2\2BC\7\61"+
		"\2\2CD\7\61\2\2D\26\3\2\2\2EF\7`\2\2FG\7`\2\2GK\3\2\2\2HJ\13\2\2\2IH\3"+
		"\2\2\2JM\3\2\2\2KL\3\2\2\2KI\3\2\2\2LN\3\2\2\2MK\3\2\2\2NO\7`\2\2OP\7"+
		"`\2\2P\30\3\2\2\2QT\5\21\t\2RT\5\17\b\2SQ\3\2\2\2SR\3\2\2\2TU\3\2\2\2"+
		"US\3\2\2\2UV\3\2\2\2V\32\3\2\2\2WY\t\5\2\2XW\3\2\2\2YZ\3\2\2\2ZX\3\2\2"+
		"\2Z[\3\2\2\2[\\\3\2\2\2\\]\b\16\2\2]\34\3\2\2\2^_\13\2\2\2_\36\3\2\2\2"+
		"\n\2/\65?KSUZ\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}