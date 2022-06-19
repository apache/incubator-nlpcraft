// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/makro/antlr4/NCMacroDsl.g4 by ANTLR 4.9.2
package org.apache.nlpcraft.internal.makro.antlr4;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCMacroDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\20l\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3"+
		"\n\3\13\3\13\3\13\3\f\5\f<\n\f\3\r\3\r\3\r\3\r\7\rB\n\r\f\r\16\rE\13\r"+
		"\3\r\3\r\3\r\3\16\3\16\3\16\3\16\7\16N\n\16\f\16\16\16Q\13\16\3\16\3\16"+
		"\3\16\3\17\3\17\6\17X\n\17\r\17\16\17Y\3\20\3\20\6\20^\n\20\r\20\16\20"+
		"_\3\20\3\20\3\21\6\21e\n\21\r\21\16\21f\3\21\3\21\3\22\3\22\4CO\2\23\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2\27\2\31\13\33\f\35\r\37\16"+
		"!\17#\20\3\2\6\7\2..\61\61]_aa}\177\23\2#\\^^`|\u0080\u0080\u00a2\u0251"+
		"\u025b\u0294\u02b2\u0371\u0402\u0501\u1e04\u1ef5\u1f03\u2001\u200e\u200f"+
		"\u2041\u2042\u2072\u2191\u2c02\u2ff1\u3003\ud801\uf902\ufdd1\ufdf2\uffff"+
		"\5\2\"\"..\62;\5\2\13\f\16\17\"\"\2n\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2"+
		"\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\31"+
		"\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2"+
		"\3%\3\2\2\2\5\'\3\2\2\2\7)\3\2\2\2\t+\3\2\2\2\13-\3\2\2\2\r/\3\2\2\2\17"+
		"\61\3\2\2\2\21\63\3\2\2\2\23\65\3\2\2\2\25\67\3\2\2\2\27;\3\2\2\2\31="+
		"\3\2\2\2\33I\3\2\2\2\35W\3\2\2\2\37[\3\2\2\2!d\3\2\2\2#j\3\2\2\2%&\7}"+
		"\2\2&\4\3\2\2\2\'(\7\177\2\2(\6\3\2\2\2)*\7~\2\2*\b\3\2\2\2+,\7.\2\2,"+
		"\n\3\2\2\2-.\7a\2\2.\f\3\2\2\2/\60\7]\2\2\60\16\3\2\2\2\61\62\7_\2\2\62"+
		"\20\3\2\2\2\63\64\7A\2\2\64\22\3\2\2\2\65\66\t\2\2\2\66\24\3\2\2\2\67"+
		"8\7^\2\289\5\23\n\29\26\3\2\2\2:<\t\3\2\2;:\3\2\2\2<\30\3\2\2\2=>\7\61"+
		"\2\2>?\7\61\2\2?C\3\2\2\2@B\13\2\2\2A@\3\2\2\2BE\3\2\2\2CD\3\2\2\2CA\3"+
		"\2\2\2DF\3\2\2\2EC\3\2\2\2FG\7\61\2\2GH\7\61\2\2H\32\3\2\2\2IJ\7`\2\2"+
		"JK\7`\2\2KO\3\2\2\2LN\13\2\2\2ML\3\2\2\2NQ\3\2\2\2OP\3\2\2\2OM\3\2\2\2"+
		"PR\3\2\2\2QO\3\2\2\2RS\7`\2\2ST\7`\2\2T\34\3\2\2\2UX\5\27\f\2VX\5\25\13"+
		"\2WU\3\2\2\2WV\3\2\2\2XY\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z\36\3\2\2\2[]\7]"+
		"\2\2\\^\t\4\2\2]\\\3\2\2\2^_\3\2\2\2_]\3\2\2\2_`\3\2\2\2`a\3\2\2\2ab\7"+
		"_\2\2b \3\2\2\2ce\t\5\2\2dc\3\2\2\2ef\3\2\2\2fd\3\2\2\2fg\3\2\2\2gh\3"+
		"\2\2\2hi\b\21\2\2i\"\3\2\2\2jk\13\2\2\2k$\3\2\2\2\n\2;COWY_f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}