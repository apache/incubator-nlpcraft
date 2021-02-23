// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/common/makro/antlr4/NCMacroDsl.g4 by ANTLR 4.9.1
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
		TXT=9, WS=10, ErrorCharacter=11;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"LCURLY", "RCURLY", "LBR", "RBR", "VERT", "COMMA", "UNDERSCORE", "ESC_CHAR", 
			"ESC", "TXT_CHAR", "INT", "TXT", "WS", "ErrorCharacter"
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
			"INT", "TXT", "WS", "ErrorCharacter"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\rN\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\n\3\13\5\13\64\n\13\3\f\3"+
		"\f\3\f\7\f9\n\f\f\f\16\f<\13\f\5\f>\n\f\3\r\3\r\6\rB\n\r\r\r\16\rC\3\16"+
		"\6\16G\n\16\r\16\16\16H\3\16\3\16\3\17\3\17\2\2\20\3\3\5\4\7\5\t\6\13"+
		"\7\r\b\17\t\21\2\23\2\25\2\27\n\31\13\33\f\35\r\3\2\7\b\2..>>@@]_aa}\177"+
		"\27\2##%(*-/@B\\^^``b|\u0080\u0080\u00b9\u00b9\u00c2\u00d8\u00da\u00f8"+
		"\u00fa\u037f\u0381\u2001\u200e\u200f\u2041\u2042\u2072\u2191\u2c02\u2ff1"+
		"\u3003\ud801\uf902\ufdd1\ufdf2\uffff\3\2\63;\4\2\62;aa\5\2\13\f\16\17"+
		"\"\"\2O\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\17\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3"+
		"\2\2\2\3\37\3\2\2\2\5!\3\2\2\2\7#\3\2\2\2\t%\3\2\2\2\13\'\3\2\2\2\r)\3"+
		"\2\2\2\17+\3\2\2\2\21-\3\2\2\2\23/\3\2\2\2\25\63\3\2\2\2\27=\3\2\2\2\31"+
		"A\3\2\2\2\33F\3\2\2\2\35L\3\2\2\2\37 \7}\2\2 \4\3\2\2\2!\"\7\177\2\2\""+
		"\6\3\2\2\2#$\7]\2\2$\b\3\2\2\2%&\7_\2\2&\n\3\2\2\2\'(\7~\2\2(\f\3\2\2"+
		"\2)*\7.\2\2*\16\3\2\2\2+,\7a\2\2,\20\3\2\2\2-.\t\2\2\2.\22\3\2\2\2/\60"+
		"\7^\2\2\60\61\5\21\t\2\61\24\3\2\2\2\62\64\t\3\2\2\63\62\3\2\2\2\64\26"+
		"\3\2\2\2\65>\7\62\2\2\66:\t\4\2\2\679\t\5\2\28\67\3\2\2\29<\3\2\2\2:8"+
		"\3\2\2\2:;\3\2\2\2;>\3\2\2\2<:\3\2\2\2=\65\3\2\2\2=\66\3\2\2\2>\30\3\2"+
		"\2\2?B\5\25\13\2@B\5\23\n\2A?\3\2\2\2A@\3\2\2\2BC\3\2\2\2CA\3\2\2\2CD"+
		"\3\2\2\2D\32\3\2\2\2EG\t\6\2\2FE\3\2\2\2GH\3\2\2\2HF\3\2\2\2HI\3\2\2\2"+
		"IJ\3\2\2\2JK\b\16\2\2K\34\3\2\2\2LM\13\2\2\2M\36\3\2\2\2\t\2\63:=ACH\3"+
		"\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}