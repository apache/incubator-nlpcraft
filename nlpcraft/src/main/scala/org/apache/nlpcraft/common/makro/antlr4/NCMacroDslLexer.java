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
		LCURLY=1, RCURLY=2, LBR=3, RBR=4, VERT=5, COMMA=6, UNDERSCORE=7, INT=8, 
		REGEX_TXT=9, DSL_TXT=10, TXT=11, WS=12, ERR_CHAR=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LCURLY", "RCURLY", "LBR", "RBR", "VERT", "COMMA", "UNDERSCORE", "ESC_CHAR", 
			"ESC", "TXT_CHAR", "INT", "REGEX_TXT", "DSL_TXT", "TXT", "WS", "ERR_CHAR"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "'['", "']'", "'|'", "','", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "LCURLY", "RCURLY", "LBR", "RBR", "VERT", "COMMA", "UNDERSCORE", 
			"INT", "REGEX_TXT", "DSL_TXT", "TXT", "WS", "ERR_CHAR"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\17j\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\3\2\3\2\3"+
		"\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13"+
		"\5\138\n\13\3\f\3\f\3\f\7\f=\n\f\f\f\16\f@\13\f\5\fB\n\f\3\r\3\r\3\r\3"+
		"\r\7\rH\n\r\f\r\16\rK\13\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\7\16T\n\16"+
		"\f\16\16\16W\13\16\3\16\3\16\3\16\3\17\3\17\6\17^\n\17\r\17\16\17_\3\20"+
		"\6\20c\n\20\r\20\16\20d\3\20\3\20\3\21\3\21\4IU\2\22\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\2\23\2\25\2\27\n\31\13\33\f\35\r\37\16!\17\3\2\7\6\2.."+
		"]_aa}\177\21\2#-/@B\\^^``b|\u0080\u0080\u00a2\u2001\u200e\u200f\u2041"+
		"\u2042\u2072\u2191\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff\3\2"+
		"\63;\4\2\62;aa\5\2\13\f\16\17\"\"\2m\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\27\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\3#\3\2\2\2"+
		"\5%\3\2\2\2\7\'\3\2\2\2\t)\3\2\2\2\13+\3\2\2\2\r-\3\2\2\2\17/\3\2\2\2"+
		"\21\61\3\2\2\2\23\63\3\2\2\2\25\67\3\2\2\2\27A\3\2\2\2\31C\3\2\2\2\33"+
		"O\3\2\2\2\35]\3\2\2\2\37b\3\2\2\2!h\3\2\2\2#$\7}\2\2$\4\3\2\2\2%&\7\177"+
		"\2\2&\6\3\2\2\2\'(\7]\2\2(\b\3\2\2\2)*\7_\2\2*\n\3\2\2\2+,\7~\2\2,\f\3"+
		"\2\2\2-.\7.\2\2.\16\3\2\2\2/\60\7a\2\2\60\20\3\2\2\2\61\62\t\2\2\2\62"+
		"\22\3\2\2\2\63\64\7^\2\2\64\65\5\21\t\2\65\24\3\2\2\2\668\t\3\2\2\67\66"+
		"\3\2\2\28\26\3\2\2\29B\7\62\2\2:>\t\4\2\2;=\t\5\2\2<;\3\2\2\2=@\3\2\2"+
		"\2><\3\2\2\2>?\3\2\2\2?B\3\2\2\2@>\3\2\2\2A9\3\2\2\2A:\3\2\2\2B\30\3\2"+
		"\2\2CD\7\61\2\2DE\7\61\2\2EI\3\2\2\2FH\13\2\2\2GF\3\2\2\2HK\3\2\2\2IJ"+
		"\3\2\2\2IG\3\2\2\2JL\3\2\2\2KI\3\2\2\2LM\7\61\2\2MN\7\61\2\2N\32\3\2\2"+
		"\2OP\7`\2\2PQ\7`\2\2QU\3\2\2\2RT\13\2\2\2SR\3\2\2\2TW\3\2\2\2UV\3\2\2"+
		"\2US\3\2\2\2VX\3\2\2\2WU\3\2\2\2XY\7`\2\2YZ\7`\2\2Z\34\3\2\2\2[^\5\25"+
		"\13\2\\^\5\23\n\2][\3\2\2\2]\\\3\2\2\2^_\3\2\2\2_]\3\2\2\2_`\3\2\2\2`"+
		"\36\3\2\2\2ac\t\6\2\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2ef\3\2\2"+
		"\2fg\b\20\2\2g \3\2\2\2hi\13\2\2\2i\"\3\2\2\2\13\2\67>AIU]_d\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}