// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, FUN_NAME=6, FRAG=7, SQSTRING=8, 
		DQSTRING=9, BOOL=10, NULL=11, EQ=12, NEQ=13, GTEQ=14, LTEQ=15, GT=16, 
		LT=17, AND=18, OR=19, VERT=20, NOT=21, LPAR=22, RPAR=23, LBRACE=24, RBRACE=25, 
		SQUOTE=26, DQUOTE=27, TILDA=28, LBR=29, RBR=30, POUND=31, COMMA=32, COLON=33, 
		MINUS=34, DOT=35, UNDERSCORE=36, ASSIGN=37, PLUS=38, QUESTION=39, MULT=40, 
		DIV=41, MOD=42, DOLLAR=43, INT=44, REAL=45, EXP=46, ID=47, COMMENT=48, 
		WS=49, ErrorChar=50;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "FUN_NAME", "FRAG", "SQSTRING", 
			"DQSTRING", "BOOL", "NULL", "EQ", "NEQ", "GTEQ", "LTEQ", "GT", "LT", 
			"AND", "OR", "VERT", "NOT", "LPAR", "RPAR", "LBRACE", "RBRACE", "SQUOTE", 
			"DQUOTE", "TILDA", "LBR", "RBR", "POUND", "COMMA", "COLON", "MINUS", 
			"DOT", "UNDERSCORE", "ASSIGN", "PLUS", "QUESTION", "MULT", "DIV", "MOD", 
			"DOLLAR", "INT", "REAL", "EXP", "UNI_CHAR", "LETTER", "ID", "COMMENT", 
			"WS", "ErrorChar"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, "'fragment'", 
			null, null, null, "'null'", "'=='", "'!='", "'>='", "'<='", "'>'", "'<'", 
			"'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", "'''", "'\"'", 
			"'~'", "'['", "']'", "'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", 
			"'+'", "'?'", "'*'", "'/'", "'%'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "FUN_NAME", "FRAG", "SQSTRING", "DQSTRING", 
			"BOOL", "NULL", "EQ", "NEQ", "GTEQ", "LTEQ", "GT", "LT", "AND", "OR", 
			"VERT", "NOT", "LPAR", "RPAR", "LBRACE", "RBRACE", "SQUOTE", "DQUOTE", 
			"TILDA", "LBR", "RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", 
			"ASSIGN", "PLUS", "QUESTION", "MULT", "DIV", "MOD", "DOLLAR", "INT", 
			"REAL", "EXP", "ID", "COMMENT", "WS", "ErrorChar"
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


	public NCIntentDslLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\64\u04b1\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t"+
		"+\4,\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64"+
		"\t\64\4\65\t\65\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3"+
		"\3\3\3\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3\7\3"+
		"\7\3\7\5\7\u03e6\n\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3"+
		"\t\7\t\u03f5\n\t\f\t\16\t\u03f8\13\t\3\t\3\t\3\n\3\n\3\n\3\n\7\n\u0400"+
		"\n\n\f\n\16\n\u0403\13\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\5\13\u0410\n\13\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16"+
		"\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\23\3\24"+
		"\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31\3\32\3\32"+
		"\3\33\3\33\3\34\3\34\3\35\3\35\3\36\3\36\3\37\3\37\3 \3 \3!\3!\3\"\3\""+
		"\3#\3#\3$\3$\3%\3%\3&\3&\3\'\3\'\3(\3(\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3"+
		"-\7-\u0460\n-\f-\16-\u0463\13-\5-\u0465\n-\3.\3.\6.\u0469\n.\r.\16.\u046a"+
		"\3/\3/\5/\u046f\n/\3/\3/\3\60\3\60\3\61\3\61\3\62\3\62\3\62\3\62\6\62"+
		"\u047b\n\62\r\62\16\62\u047c\3\62\3\62\3\62\3\62\3\62\3\62\3\62\7\62\u0486"+
		"\n\62\f\62\16\62\u0489\13\62\3\63\3\63\3\63\3\63\7\63\u048f\n\63\f\63"+
		"\16\63\u0492\13\63\3\63\5\63\u0495\n\63\3\63\5\63\u0498\n\63\3\63\3\63"+
		"\3\63\3\63\7\63\u049e\n\63\f\63\16\63\u04a1\13\63\3\63\3\63\5\63\u04a5"+
		"\n\63\3\63\3\63\3\64\6\64\u04aa\n\64\r\64\16\64\u04ab\3\64\3\64\3\65\3"+
		"\65\3\u049f\2\66\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65"+
		"\34\67\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\2a\2c\61e\62"+
		"g\63i\64\3\2\16\3\2))\3\2$$\3\2\63;\4\2\62;aa\3\2\62;\4\2GGgg\4\2--//"+
		"\17\2\u00a2\u0251\u025b\u0294\u02b2\u0371\u0402\u0501\u1e04\u1ef5\u1f03"+
		"\u2001\u200e\u200f\u2041\u2042\u2072\u2191\u2c02\u2ff1\u3003\ud801\uf902"+
		"\ufdd1\ufdf2\uffff\4\2C\\c|\4\2\f\f\17\17\3\3\f\f\5\2\13\f\16\17\"\"\2"+
		"\u0547\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3"+
		"\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2"+
		"\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2"+
		"/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2"+
		"\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2"+
		"G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3"+
		"\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2c\3\2\2"+
		"\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\3k\3\2\2\2\5r\3\2\2\2\7z\3\2\2\2\t"+
		"\177\3\2\2\2\13\u0084\3\2\2\2\r\u03e5\3\2\2\2\17\u03e7\3\2\2\2\21\u03f0"+
		"\3\2\2\2\23\u03fb\3\2\2\2\25\u040f\3\2\2\2\27\u0411\3\2\2\2\31\u0416\3"+
		"\2\2\2\33\u0419\3\2\2\2\35\u041c\3\2\2\2\37\u041f\3\2\2\2!\u0422\3\2\2"+
		"\2#\u0424\3\2\2\2%\u0426\3\2\2\2\'\u0429\3\2\2\2)\u042c\3\2\2\2+\u042e"+
		"\3\2\2\2-\u0430\3\2\2\2/\u0432\3\2\2\2\61\u0434\3\2\2\2\63\u0436\3\2\2"+
		"\2\65\u0438\3\2\2\2\67\u043a\3\2\2\29\u043c\3\2\2\2;\u043e\3\2\2\2=\u0440"+
		"\3\2\2\2?\u0442\3\2\2\2A\u0444\3\2\2\2C\u0446\3\2\2\2E\u0448\3\2\2\2G"+
		"\u044a\3\2\2\2I\u044c\3\2\2\2K\u044e\3\2\2\2M\u0450\3\2\2\2O\u0452\3\2"+
		"\2\2Q\u0454\3\2\2\2S\u0456\3\2\2\2U\u0458\3\2\2\2W\u045a\3\2\2\2Y\u0464"+
		"\3\2\2\2[\u0466\3\2\2\2]\u046c\3\2\2\2_\u0472\3\2\2\2a\u0474\3\2\2\2c"+
		"\u047a\3\2\2\2e\u04a4\3\2\2\2g\u04a9\3\2\2\2i\u04af\3\2\2\2kl\7k\2\2l"+
		"m\7p\2\2mn\7v\2\2no\7g\2\2op\7p\2\2pq\7v\2\2q\4\3\2\2\2rs\7q\2\2st\7t"+
		"\2\2tu\7f\2\2uv\7g\2\2vw\7t\2\2wx\7g\2\2xy\7f\2\2y\6\3\2\2\2z{\7h\2\2"+
		"{|\7n\2\2|}\7q\2\2}~\7y\2\2~\b\3\2\2\2\177\u0080\7o\2\2\u0080\u0081\7"+
		"g\2\2\u0081\u0082\7v\2\2\u0082\u0083\7c\2\2\u0083\n\3\2\2\2\u0084\u0085"+
		"\7v\2\2\u0085\u0086\7g\2\2\u0086\u0087\7t\2\2\u0087\u0088\7o\2\2\u0088"+
		"\f\3\2\2\2\u0089\u008a\7o\2\2\u008a\u008b\7g\2\2\u008b\u008c\7v\2\2\u008c"+
		"\u008d\7c\2\2\u008d\u008e\7a\2\2\u008e\u008f\7v\2\2\u008f\u0090\7q\2\2"+
		"\u0090\u0091\7m\2\2\u0091\u0092\7g\2\2\u0092\u03e6\7p\2\2\u0093\u0094"+
		"\7o\2\2\u0094\u0095\7g\2\2\u0095\u0096\7v\2\2\u0096\u0097\7c\2\2\u0097"+
		"\u0098\7a\2\2\u0098\u0099\7r\2\2\u0099\u009a\7c\2\2\u009a\u009b\7t\2\2"+
		"\u009b\u03e6\7v\2\2\u009c\u009d\7o\2\2\u009d\u009e\7g\2\2\u009e\u009f"+
		"\7v\2\2\u009f\u00a0\7c\2\2\u00a0\u00a1\7a\2\2\u00a1\u00a2\7o\2\2\u00a2"+
		"\u00a3\7q\2\2\u00a3\u00a4\7f\2\2\u00a4\u00a5\7g\2\2\u00a5\u03e6\7n\2\2"+
		"\u00a6\u00a7\7o\2\2\u00a7\u00a8\7g\2\2\u00a8\u00a9\7v\2\2\u00a9\u00aa"+
		"\7c\2\2\u00aa\u00ab\7a\2\2\u00ab\u00ac\7k\2\2\u00ac\u00ad\7p\2\2\u00ad"+
		"\u00ae\7v\2\2\u00ae\u00af\7g\2\2\u00af\u00b0\7p\2\2\u00b0\u03e6\7v\2\2"+
		"\u00b1\u00b2\7o\2\2\u00b2\u00b3\7g\2\2\u00b3\u00b4\7v\2\2\u00b4\u00b5"+
		"\7c\2\2\u00b5\u00b6\7a\2\2\u00b6\u00b7\7t\2\2\u00b7\u00b8\7g\2\2\u00b8"+
		"\u03e6\7s\2\2\u00b9\u00ba\7o\2\2\u00ba\u00bb\7g\2\2\u00bb\u00bc\7v\2\2"+
		"\u00bc\u00bd\7c\2\2\u00bd\u00be\7a\2\2\u00be\u00bf\7w\2\2\u00bf\u00c0"+
		"\7u\2\2\u00c0\u00c1\7g\2\2\u00c1\u03e6\7t\2\2\u00c2\u00c3\7o\2\2\u00c3"+
		"\u00c4\7g\2\2\u00c4\u00c5\7v\2\2\u00c5\u00c6\7c\2\2\u00c6\u00c7\7a\2\2"+
		"\u00c7\u00c8\7e\2\2\u00c8\u00c9\7q\2\2\u00c9\u00ca\7o\2\2\u00ca\u00cb"+
		"\7r\2\2\u00cb\u00cc\7c\2\2\u00cc\u00cd\7p\2\2\u00cd\u03e6\7{\2\2\u00ce"+
		"\u00cf\7o\2\2\u00cf\u00d0\7g\2\2\u00d0\u00d1\7v\2\2\u00d1\u00d2\7c\2\2"+
		"\u00d2\u00d3\7a\2\2\u00d3\u00d4\7u\2\2\u00d4\u00d5\7{\2\2\u00d5\u03e6"+
		"\7u\2\2\u00d6\u00d7\7o\2\2\u00d7\u00d8\7g\2\2\u00d8\u00d9\7v\2\2\u00d9"+
		"\u00da\7c\2\2\u00da\u00db\7a\2\2\u00db\u00dc\7e\2\2\u00dc\u00dd\7q\2\2"+
		"\u00dd\u00de\7p\2\2\u00de\u03e6\7x\2\2\u00df\u00e0\7o\2\2\u00e0\u00e1"+
		"\7g\2\2\u00e1\u00e2\7v\2\2\u00e2\u00e3\7c\2\2\u00e3\u00e4\7a\2\2\u00e4"+
		"\u00e5\7h\2\2\u00e5\u00e6\7t\2\2\u00e6\u00e7\7c\2\2\u00e7\u03e6\7i\2\2"+
		"\u00e8\u00e9\7l\2\2\u00e9\u00ea\7u\2\2\u00ea\u00eb\7q\2\2\u00eb\u03e6"+
		"\7p\2\2\u00ec\u00ed\7k\2\2\u00ed\u03e6\7h\2\2\u00ee\u00ef\7k\2\2\u00ef"+
		"\u03e6\7f\2\2\u00f0\u00f1\7v\2\2\u00f1\u00f2\7j\2\2\u00f2\u00f3\7k\2\2"+
		"\u00f3\u03e6\7u\2\2\u00f4\u00f5\7r\2\2\u00f5\u00f6\7c\2\2\u00f6\u00f7"+
		"\7t\2\2\u00f7\u03e6\7v\2\2\u00f8\u00f9\7r\2\2\u00f9\u00fa\7c\2\2\u00fa"+
		"\u00fb\7t\2\2\u00fb\u00fc\7v\2\2\u00fc\u03e6\7u\2\2\u00fd\u00fe\7c\2\2"+
		"\u00fe\u00ff\7p\2\2\u00ff\u0100\7e\2\2\u0100\u0101\7g\2\2\u0101\u0102"+
		"\7u\2\2\u0102\u0103\7v\2\2\u0103\u0104\7q\2\2\u0104\u0105\7t\2\2\u0105"+
		"\u03e6\7u\2\2\u0106\u0107\7r\2\2\u0107\u0108\7c\2\2\u0108\u0109\7t\2\2"+
		"\u0109\u010a\7g\2\2\u010a\u010b\7p\2\2\u010b\u03e6\7v\2\2\u010c\u010d"+
		"\7i\2\2\u010d\u010e\7t\2\2\u010e\u010f\7q\2\2\u010f\u0110\7w\2\2\u0110"+
		"\u0111\7r\2\2\u0111\u03e6\7u\2\2\u0112\u0113\7x\2\2\u0113\u0114\7c\2\2"+
		"\u0114\u0115\7n\2\2\u0115\u0116\7w\2\2\u0116\u03e6\7g\2\2\u0117\u0118"+
		"\7c\2\2\u0118\u0119\7n\2\2\u0119\u011a\7k\2\2\u011a\u011b\7c\2\2\u011b"+
		"\u011c\7u\2\2\u011c\u011d\7g\2\2\u011d\u03e6\7u\2\2\u011e\u011f\7u\2\2"+
		"\u011f\u0120\7v\2\2\u0120\u0121\7c\2\2\u0121\u0122\7t\2\2\u0122\u0123"+
		"\7v\2\2\u0123\u0124\7a\2\2\u0124\u0125\7k\2\2\u0125\u0126\7f\2\2\u0126"+
		"\u03e6\7z\2\2\u0127\u0128\7g\2\2\u0128\u0129\7p\2\2\u0129\u012a\7f\2\2"+
		"\u012a\u012b\7a\2\2\u012b\u012c\7k\2\2\u012c\u012d\7f\2\2\u012d\u03e6"+
		"\7z\2\2\u012e\u012f\7t\2\2\u012f\u0130\7g\2\2\u0130\u0131\7s\2\2\u0131"+
		"\u0132\7a\2\2\u0132\u0133\7k\2\2\u0133\u03e6\7f\2\2\u0134\u0135\7t\2\2"+
		"\u0135\u0136\7g\2\2\u0136\u0137\7s\2\2\u0137\u0138\7a\2\2\u0138\u0139"+
		"\7p\2\2\u0139\u013a\7q\2\2\u013a\u013b\7t\2\2\u013b\u013c\7o\2\2\u013c"+
		"\u013d\7v\2\2\u013d\u013e\7g\2\2\u013e\u013f\7z\2\2\u013f\u03e6\7v\2\2"+
		"\u0140\u0141\7t\2\2\u0141\u0142\7g\2\2\u0142\u0143\7s\2\2\u0143\u0144"+
		"\7a\2\2\u0144\u0145\7v\2\2\u0145\u0146\7u\2\2\u0146\u0147\7v\2\2\u0147"+
		"\u0148\7c\2\2\u0148\u0149\7o\2\2\u0149\u03e6\7r\2\2\u014a\u014b\7t\2\2"+
		"\u014b\u014c\7g\2\2\u014c\u014d\7s\2\2\u014d\u014e\7a\2\2\u014e\u014f"+
		"\7c\2\2\u014f\u0150\7f\2\2\u0150\u0151\7f\2\2\u0151\u03e6\7t\2\2\u0152"+
		"\u0153\7t\2\2\u0153\u0154\7g\2\2\u0154\u0155\7s\2\2\u0155\u0156\7a\2\2"+
		"\u0156\u0157\7c\2\2\u0157\u0158\7i\2\2\u0158\u0159\7g\2\2\u0159\u015a"+
		"\7p\2\2\u015a\u03e6\7v\2\2\u015b\u015c\7w\2\2\u015c\u015d\7u\2\2\u015d"+
		"\u015e\7g\2\2\u015e\u015f\7t\2\2\u015f\u0160\7a\2\2\u0160\u0161\7k\2\2"+
		"\u0161\u03e6\7f\2\2\u0162\u0163\7w\2\2\u0163\u0164\7u\2\2\u0164\u0165"+
		"\7g\2\2\u0165\u0166\7t\2\2\u0166\u0167\7a\2\2\u0167\u0168\7h\2\2\u0168"+
		"\u0169\7p\2\2\u0169\u016a\7c\2\2\u016a\u016b\7o\2\2\u016b\u03e6\7g\2\2"+
		"\u016c\u016d\7w\2\2\u016d\u016e\7u\2\2\u016e\u016f\7g\2\2\u016f\u0170"+
		"\7t\2\2\u0170\u0171\7a\2\2\u0171\u0172\7n\2\2\u0172\u0173\7p\2\2\u0173"+
		"\u0174\7c\2\2\u0174\u0175\7o\2\2\u0175\u03e6\7g\2\2\u0176\u0177\7w\2\2"+
		"\u0177\u0178\7u\2\2\u0178\u0179\7g\2\2\u0179\u017a\7t\2\2\u017a\u017b"+
		"\7a\2\2\u017b\u017c\7g\2\2\u017c\u017d\7o\2\2\u017d\u017e\7c\2\2\u017e"+
		"\u017f\7k\2\2\u017f\u03e6\7n\2\2\u0180\u0181\7w\2\2\u0181\u0182\7u\2\2"+
		"\u0182\u0183\7g\2\2\u0183\u0184\7t\2\2\u0184\u0185\7a\2\2\u0185\u0186"+
		"\7c\2\2\u0186\u0187\7f\2\2\u0187\u0188\7o\2\2\u0188\u0189\7k\2\2\u0189"+
		"\u03e6\7p\2\2\u018a\u018b\7w\2\2\u018b\u018c\7u\2\2\u018c\u018d\7g\2\2"+
		"\u018d\u018e\7t\2\2\u018e\u018f\7a\2\2\u018f\u0190\7u\2\2\u0190\u0191"+
		"\7k\2\2\u0191\u0192\7i\2\2\u0192\u0193\7p\2\2\u0193\u0194\7w\2\2\u0194"+
		"\u0195\7r\2\2\u0195\u0196\7a\2\2\u0196\u0197\7v\2\2\u0197\u0198\7u\2\2"+
		"\u0198\u0199\7v\2\2\u0199\u019a\7c\2\2\u019a\u019b\7o\2\2\u019b\u03e6"+
		"\7r\2\2\u019c\u019d\7e\2\2\u019d\u019e\7q\2\2\u019e\u019f\7o\2\2\u019f"+
		"\u01a0\7r\2\2\u01a0\u01a1\7a\2\2\u01a1\u01a2\7k\2\2\u01a2\u03e6\7f\2\2"+
		"\u01a3\u01a4\7e\2\2\u01a4\u01a5\7q\2\2\u01a5\u01a6\7o\2\2\u01a6\u01a7"+
		"\7r\2\2\u01a7\u01a8\7a\2\2\u01a8\u01a9\7p\2\2\u01a9\u01aa\7c\2\2\u01aa"+
		"\u01ab\7o\2\2\u01ab\u03e6\7g\2\2\u01ac\u01ad\7e\2\2\u01ad\u01ae\7q\2\2"+
		"\u01ae\u01af\7o\2\2\u01af\u01b0\7r\2\2\u01b0\u01b1\7a\2\2\u01b1\u01b2"+
		"\7y\2\2\u01b2\u01b3\7g\2\2\u01b3\u01b4\7d\2\2\u01b4\u01b5\7u\2\2\u01b5"+
		"\u01b6\7k\2\2\u01b6\u01b7\7v\2\2\u01b7\u03e6\7g\2\2\u01b8\u01b9\7e\2\2"+
		"\u01b9\u01ba\7q\2\2\u01ba\u01bb\7o\2\2\u01bb\u01bc\7r\2\2\u01bc\u01bd"+
		"\7a\2\2\u01bd\u01be\7e\2\2\u01be\u01bf\7q\2\2\u01bf\u01c0\7w\2\2\u01c0"+
		"\u01c1\7p\2\2\u01c1\u01c2\7v\2\2\u01c2\u01c3\7t\2\2\u01c3\u03e6\7{\2\2"+
		"\u01c4\u01c5\7e\2\2\u01c5\u01c6\7q\2\2\u01c6\u01c7\7o\2\2\u01c7\u01c8"+
		"\7r\2\2\u01c8\u01c9\7a\2\2\u01c9\u01ca\7t\2\2\u01ca\u01cb\7g\2\2\u01cb"+
		"\u01cc\7i\2\2\u01cc\u01cd\7k\2\2\u01cd\u01ce\7q\2\2\u01ce\u03e6\7p\2\2"+
		"\u01cf\u01d0\7e\2\2\u01d0\u01d1\7q\2\2\u01d1\u01d2\7o\2\2\u01d2\u01d3"+
		"\7r\2\2\u01d3\u01d4\7a\2\2\u01d4\u01d5\7e\2\2\u01d5\u01d6\7k\2\2\u01d6"+
		"\u01d7\7v\2\2\u01d7\u03e6\7{\2\2\u01d8\u01d9\7e\2\2\u01d9\u01da\7q\2\2"+
		"\u01da\u01db\7o\2\2\u01db\u01dc\7r\2\2\u01dc\u01dd\7a\2\2\u01dd\u01de"+
		"\7c\2\2\u01de\u01df\7f\2\2\u01df\u01e0\7f\2\2\u01e0\u03e6\7t\2\2\u01e1"+
		"\u01e2\7e\2\2\u01e2\u01e3\7q\2\2\u01e3\u01e4\7o\2\2\u01e4\u01e5\7r\2\2"+
		"\u01e5\u01e6\7a\2\2\u01e6\u01e7\7r\2\2\u01e7\u01e8\7q\2\2\u01e8\u01e9"+
		"\7u\2\2\u01e9\u01ea\7v\2\2\u01ea\u01eb\7e\2\2\u01eb\u01ec\7q\2\2\u01ec"+
		"\u01ed\7f\2\2\u01ed\u03e6\7g\2\2\u01ee\u01ef\7v\2\2\u01ef\u01f0\7t\2\2"+
		"\u01f0\u01f1\7k\2\2\u01f1\u03e6\7o\2\2\u01f2\u01f3\7u\2\2\u01f3\u01f4"+
		"\7v\2\2\u01f4\u01f5\7t\2\2\u01f5\u01f6\7k\2\2\u01f6\u03e6\7r\2\2\u01f7"+
		"\u01f8\7w\2\2\u01f8\u01f9\7r\2\2\u01f9\u01fa\7r\2\2\u01fa\u01fb\7g\2\2"+
		"\u01fb\u01fc\7t\2\2\u01fc\u01fd\7e\2\2\u01fd\u01fe\7c\2\2\u01fe\u01ff"+
		"\7u\2\2\u01ff\u03e6\7g\2\2\u0200\u0201\7n\2\2\u0201\u0202\7q\2\2\u0202"+
		"\u0203\7y\2\2\u0203\u0204\7g\2\2\u0204\u0205\7t\2\2\u0205\u0206\7e\2\2"+
		"\u0206\u0207\7c\2\2\u0207\u0208\7u\2\2\u0208\u03e6\7g\2\2\u0209\u020a"+
		"\7k\2\2\u020a\u020b\7u\2\2\u020b\u020c\7a\2\2\u020c\u020d\7c\2\2\u020d"+
		"\u020e\7n\2\2\u020e\u020f\7r\2\2\u020f\u0210\7j\2\2\u0210\u03e6\7c\2\2"+
		"\u0211\u0212\7k\2\2\u0212\u0213\7u\2\2\u0213\u0214\7a\2\2\u0214\u0215"+
		"\7c\2\2\u0215\u0216\7n\2\2\u0216\u0217\7r\2\2\u0217\u0218\7j\2\2\u0218"+
		"\u0219\7c\2\2\u0219\u021a\7p\2\2\u021a\u021b\7w\2\2\u021b\u03e6\7o\2\2"+
		"\u021c\u021d\7k\2\2\u021d\u021e\7u\2\2\u021e\u021f\7a\2\2\u021f\u0220"+
		"\7y\2\2\u0220\u0221\7j\2\2\u0221\u0222\7k\2\2\u0222\u0223\7v\2\2\u0223"+
		"\u0224\7g\2\2\u0224\u0225\7u\2\2\u0225\u0226\7r\2\2\u0226\u0227\7c\2\2"+
		"\u0227\u0228\7e\2\2\u0228\u03e6\7g\2\2\u0229\u022a\7k\2\2\u022a\u022b"+
		"\7u\2\2\u022b\u022c\7a\2\2\u022c\u022d\7p\2\2\u022d\u022e\7w\2\2\u022e"+
		"\u03e6\7o\2\2\u022f\u0230\7k\2\2\u0230\u0231\7u\2\2\u0231\u0232\7a\2\2"+
		"\u0232\u0233\7p\2\2\u0233\u0234\7w\2\2\u0234\u0235\7o\2\2\u0235\u0236"+
		"\7u\2\2\u0236\u0237\7r\2\2\u0237\u0238\7c\2\2\u0238\u0239\7e\2\2\u0239"+
		"\u03e6\7g\2\2\u023a\u023b\7k\2\2\u023b\u023c\7u\2\2\u023c\u023d\7a\2\2"+
		"\u023d\u023e\7c\2\2\u023e\u023f\7n\2\2\u023f\u0240\7r\2\2\u0240\u0241"+
		"\7j\2\2\u0241\u0242\7c\2\2\u0242\u0243\7u\2\2\u0243\u0244\7r\2\2\u0244"+
		"\u0245\7c\2\2\u0245\u0246\7e\2\2\u0246\u03e6\7g\2\2\u0247\u0248\7k\2\2"+
		"\u0248\u0249\7u\2\2\u0249\u024a\7a\2\2\u024a\u024b\7c\2\2\u024b\u024c"+
		"\7n\2\2\u024c\u024d\7r\2\2\u024d\u024e\7j\2\2\u024e\u024f\7c\2\2\u024f"+
		"\u0250\7p\2\2\u0250\u0251\7w\2\2\u0251\u0252\7o\2\2\u0252\u0253\7u\2\2"+
		"\u0253\u0254\7r\2\2\u0254\u0255\7c\2\2\u0255\u0256\7e\2\2\u0256\u03e6"+
		"\7g\2\2\u0257\u0258\7u\2\2\u0258\u0259\7w\2\2\u0259\u025a\7d\2\2\u025a"+
		"\u025b\7u\2\2\u025b\u025c\7v\2\2\u025c\u025d\7t\2\2\u025d\u025e\7k\2\2"+
		"\u025e\u025f\7p\2\2\u025f\u03e6\7i\2\2\u0260\u0261\7e\2\2\u0261\u0262"+
		"\7j\2\2\u0262\u0263\7c\2\2\u0263\u0264\7t\2\2\u0264\u0265\7C\2\2\u0265"+
		"\u03e6\7v\2\2\u0266\u0267\7t\2\2\u0267\u0268\7g\2\2\u0268\u0269\7i\2\2"+
		"\u0269\u026a\7g\2\2\u026a\u03e6\7z\2\2\u026b\u026c\7u\2\2\u026c\u026d"+
		"\7q\2\2\u026d\u026e\7w\2\2\u026e\u026f\7p\2\2\u026f\u0270\7f\2\2\u0270"+
		"\u0271\7g\2\2\u0271\u03e6\7z\2\2\u0272\u0273\7u\2\2\u0273\u0274\7r\2\2"+
		"\u0274\u0275\7n\2\2\u0275\u0276\7k\2\2\u0276\u03e6\7v\2\2\u0277\u0278"+
		"\7u\2\2\u0278\u0279\7r\2\2\u0279\u027a\7n\2\2\u027a\u027b\7k\2\2\u027b"+
		"\u027c\7v\2\2\u027c\u027d\7a\2\2\u027d\u027e\7v\2\2\u027e\u027f\7t\2\2"+
		"\u027f\u0280\7k\2\2\u0280\u03e6\7o\2\2\u0281\u0282\7t\2\2\u0282\u0283"+
		"\7g\2\2\u0283\u0284\7r\2\2\u0284\u0285\7n\2\2\u0285\u0286\7c\2\2\u0286"+
		"\u0287\7e\2\2\u0287\u03e6\7g\2\2\u0288\u0289\7c\2\2\u0289\u028a\7d\2\2"+
		"\u028a\u03e6\7u\2\2\u028b\u028c\7e\2\2\u028c\u028d\7g\2\2\u028d\u028e"+
		"\7k\2\2\u028e\u03e6\7n\2\2\u028f\u0290\7h\2\2\u0290\u0291\7n\2\2\u0291"+
		"\u0292\7q\2\2\u0292\u0293\7q\2\2\u0293\u03e6\7t\2\2\u0294\u0295\7t\2\2"+
		"\u0295\u0296\7k\2\2\u0296\u0297\7p\2\2\u0297\u03e6\7v\2\2\u0298\u0299"+
		"\7t\2\2\u0299\u029a\7q\2\2\u029a\u029b\7w\2\2\u029b\u029c\7p\2\2\u029c"+
		"\u03e6\7f\2\2\u029d\u029e\7u\2\2\u029e\u029f\7k\2\2\u029f\u02a0\7i\2\2"+
		"\u02a0\u02a1\7p\2\2\u02a1\u02a2\7w\2\2\u02a2\u03e6\7o\2\2\u02a3\u02a4"+
		"\7u\2\2\u02a4\u02a5\7s\2\2\u02a5\u02a6\7t\2\2\u02a6\u03e6\7v\2\2\u02a7"+
		"\u02a8\7e\2\2\u02a8\u02a9\7d\2\2\u02a9\u02aa\7t\2\2\u02aa\u03e6\7v\2\2"+
		"\u02ab\u02ac\7r\2\2\u02ac\u03e6\7k\2\2\u02ad\u02ae\7g\2\2\u02ae\u02af"+
		"\7w\2\2\u02af\u02b0\7n\2\2\u02b0\u02b1\7g\2\2\u02b1\u03e6\7t\2\2\u02b2"+
		"\u02b3\7c\2\2\u02b3\u02b4\7e\2\2\u02b4\u02b5\7q\2\2\u02b5\u03e6\7u\2\2"+
		"\u02b6\u02b7\7c\2\2\u02b7\u02b8\7u\2\2\u02b8\u02b9\7k\2\2\u02b9\u03e6"+
		"\7p\2\2\u02ba\u02bb\7c\2\2\u02bb\u02bc\7v\2\2\u02bc\u02bd\7c\2\2\u02bd"+
		"\u03e6\7p\2\2\u02be\u02bf\7e\2\2\u02bf\u02c0\7q\2\2\u02c0\u03e6\7u\2\2"+
		"\u02c1\u02c2\7u\2\2\u02c2\u02c3\7k\2\2\u02c3\u03e6\7p\2\2\u02c4\u02c5"+
		"\7v\2\2\u02c5\u02c6\7c\2\2\u02c6\u03e6\7p\2\2\u02c7\u02c8\7e\2\2\u02c8"+
		"\u02c9\7q\2\2\u02c9\u02ca\7u\2\2\u02ca\u03e6\7j\2\2\u02cb\u02cc\7u\2\2"+
		"\u02cc\u02cd\7k\2\2\u02cd\u02ce\7p\2\2\u02ce\u03e6\7j\2\2\u02cf\u02d0"+
		"\7v\2\2\u02d0\u02d1\7c\2\2\u02d1\u02d2\7p\2\2\u02d2\u03e6\7j\2\2\u02d3"+
		"\u02d4\7c\2\2\u02d4\u02d5\7v\2\2\u02d5\u02d6\7p\2\2\u02d6\u03e6\7\64\2"+
		"\2\u02d7\u02d8\7f\2\2\u02d8\u02d9\7g\2\2\u02d9\u02da\7i\2\2\u02da\u02db"+
		"\7t\2\2\u02db\u02dc\7g\2\2\u02dc\u02dd\7g\2\2\u02dd\u03e6\7u\2\2\u02de"+
		"\u02df\7t\2\2\u02df\u02e0\7c\2\2\u02e0\u02e1\7f\2\2\u02e1\u02e2\7k\2\2"+
		"\u02e2\u02e3\7c\2\2\u02e3\u02e4\7p\2\2\u02e4\u03e6\7u\2\2\u02e5\u02e6"+
		"\7g\2\2\u02e6\u02e7\7z\2\2\u02e7\u03e6\7r\2\2\u02e8\u02e9\7g\2\2\u02e9"+
		"\u02ea\7z\2\2\u02ea\u02eb\7r\2\2\u02eb\u02ec\7o\2\2\u02ec\u03e6\7\63\2"+
		"\2\u02ed\u02ee\7j\2\2\u02ee\u02ef\7{\2\2\u02ef\u02f0\7r\2\2\u02f0\u02f1"+
		"\7q\2\2\u02f1\u03e6\7v\2\2\u02f2\u02f3\7n\2\2\u02f3\u02f4\7q\2\2\u02f4"+
		"\u03e6\7i\2\2\u02f5\u02f6\7n\2\2\u02f6\u02f7\7q\2\2\u02f7\u02f8\7i\2\2"+
		"\u02f8\u02f9\7\63\2\2\u02f9\u03e6\7\62\2\2\u02fa\u02fb\7n\2\2\u02fb\u02fc"+
		"\7q\2\2\u02fc\u02fd\7i\2\2\u02fd\u02fe\7\63\2\2\u02fe\u03e6\7r\2\2\u02ff"+
		"\u0300\7r\2\2\u0300\u0301\7q\2\2\u0301\u03e6\7y\2\2\u0302\u0303\7t\2\2"+
		"\u0303\u0304\7c\2\2\u0304\u0305\7p\2\2\u0305\u03e6\7f\2\2\u0306\u0307"+
		"\7u\2\2\u0307\u0308\7s\2\2\u0308\u0309\7w\2\2\u0309\u030a\7c\2\2\u030a"+
		"\u030b\7t\2\2\u030b\u03e6\7g\2\2\u030c\u030d\7n\2\2\u030d\u030e\7k\2\2"+
		"\u030e\u030f\7u\2\2\u030f\u03e6\7v\2\2\u0310\u0311\7o\2\2\u0311\u0312"+
		"\7c\2\2\u0312\u03e6\7r\2\2\u0313\u0314\7i\2\2\u0314\u0315\7g\2\2\u0315"+
		"\u03e6\7v\2\2\u0316\u0317\7k\2\2\u0317\u0318\7p\2\2\u0318\u0319\7f\2\2"+
		"\u0319\u031a\7g\2\2\u031a\u03e6\7z\2\2\u031b\u031c\7j\2\2\u031c\u031d"+
		"\7c\2\2\u031d\u03e6\7u\2\2\u031e\u031f\7v\2\2\u031f\u0320\7c\2\2\u0320"+
		"\u0321\7k\2\2\u0321\u03e6\7n\2\2\u0322\u0323\7c\2\2\u0323\u0324\7f\2\2"+
		"\u0324\u03e6\7f\2\2\u0325\u0326\7t\2\2\u0326\u0327\7g\2\2\u0327\u0328"+
		"\7o\2\2\u0328\u0329\7q\2\2\u0329\u032a\7x\2\2\u032a\u03e6\7g\2\2\u032b"+
		"\u032c\7h\2\2\u032c\u032d\7k\2\2\u032d\u032e\7t\2\2\u032e\u032f\7u\2\2"+
		"\u032f\u03e6\7v\2\2\u0330\u0331\7n\2\2\u0331\u0332\7c\2\2\u0332\u0333"+
		"\7u\2\2\u0333\u03e6\7v\2\2\u0334\u0335\7m\2\2\u0335\u0336\7g\2\2\u0336"+
		"\u0337\7{\2\2\u0337\u03e6\7u\2\2\u0338\u0339\7x\2\2\u0339\u033a\7c\2\2"+
		"\u033a\u033b\7n\2\2\u033b\u033c\7w\2\2\u033c\u033d\7g\2\2\u033d\u03e6"+
		"\7u\2\2\u033e\u033f\7n\2\2\u033f\u0340\7g\2\2\u0340\u0341\7p\2\2\u0341"+
		"\u0342\7i\2\2\u0342\u0343\7v\2\2\u0343\u03e6\7j\2\2\u0344\u0345\7e\2\2"+
		"\u0345\u0346\7q\2\2\u0346\u0347\7w\2\2\u0347\u0348\7p\2\2\u0348\u03e6"+
		"\7v\2\2\u0349\u034a\7v\2\2\u034a\u034b\7c\2\2\u034b\u034c\7m\2\2\u034c"+
		"\u03e6\7g\2\2\u034d\u034e\7f\2\2\u034e\u034f\7t\2\2\u034f\u0350\7q\2\2"+
		"\u0350\u03e6\7r\2\2\u0351\u0352\7u\2\2\u0352\u0353\7k\2\2\u0353\u0354"+
		"\7|\2\2\u0354\u03e6\7g\2\2\u0355\u0356\7t\2\2\u0356\u0357\7g\2\2\u0357"+
		"\u0358\7x\2\2\u0358\u0359\7g\2\2\u0359\u035a\7t\2\2\u035a\u035b\7u\2\2"+
		"\u035b\u03e6\7g\2\2\u035c\u035d\7k\2\2\u035d\u035e\7u\2\2\u035e\u035f"+
		"\7a\2\2\u035f\u0360\7g\2\2\u0360\u0361\7o\2\2\u0361\u0362\7r\2\2\u0362"+
		"\u0363\7v\2\2\u0363\u03e6\7{\2\2\u0364\u0365\7p\2\2\u0365\u0366\7q\2\2"+
		"\u0366\u0367\7p\2\2\u0367\u0368\7a\2\2\u0368\u0369\7g\2\2\u0369\u036a"+
		"\7o\2\2\u036a\u036b\7r\2\2\u036b\u036c\7v\2\2\u036c\u03e6\7{\2\2\u036d"+
		"\u036e\7v\2\2\u036e\u036f\7q\2\2\u036f\u0370\7a\2\2\u0370\u0371\7u\2\2"+
		"\u0371\u0372\7v\2\2\u0372\u0373\7t\2\2\u0373\u0374\7k\2\2\u0374\u0375"+
		"\7p\2\2\u0375\u03e6\7i\2\2\u0376\u0377\7c\2\2\u0377\u0378\7x\2\2\u0378"+
		"\u03e6\7i\2\2\u0379\u037a\7o\2\2\u037a\u037b\7c\2\2\u037b\u03e6\7z\2\2"+
		"\u037c\u037d\7o\2\2\u037d\u037e\7k\2\2\u037e\u03e6\7p\2\2\u037f\u0380"+
		"\7u\2\2\u0380\u0381\7v\2\2\u0381\u0382\7f\2\2\u0382\u0383\7g\2\2\u0383"+
		"\u03e6\7x\2\2\u0384\u0385\7u\2\2\u0385\u0386\7w\2\2\u0386\u03e6\7o\2\2"+
		"\u0387\u0388\7{\2\2\u0388\u0389\7g\2\2\u0389\u038a\7c\2\2\u038a\u03e6"+
		"\7t\2\2\u038b\u038c\7o\2\2\u038c\u038d\7q\2\2\u038d\u038e\7p\2\2\u038e"+
		"\u038f\7v\2\2\u038f\u03e6\7j\2\2\u0390\u0391\7f\2\2\u0391\u0392\7c\2\2"+
		"\u0392\u0393\7{\2\2\u0393\u0394\7a\2\2\u0394\u0395\7q\2\2\u0395\u0396"+
		"\7h\2\2\u0396\u0397\7a\2\2\u0397\u0398\7o\2\2\u0398\u0399\7q\2\2\u0399"+
		"\u039a\7p\2\2\u039a\u039b\7v\2\2\u039b\u03e6\7j\2\2\u039c\u039d\7f\2\2"+
		"\u039d\u039e\7c\2\2\u039e\u039f\7{\2\2\u039f\u03a0\7a\2\2\u03a0\u03a1"+
		"\7q\2\2\u03a1\u03a2\7h\2\2\u03a2\u03a3\7a\2\2\u03a3\u03a4\7y\2\2\u03a4"+
		"\u03a5\7g\2\2\u03a5\u03a6\7g\2\2\u03a6\u03e6\7m\2\2\u03a7\u03a8\7f\2\2"+
		"\u03a8\u03a9\7c\2\2\u03a9\u03aa\7{\2\2\u03aa\u03ab\7a\2\2\u03ab\u03ac"+
		"\7q\2\2\u03ac\u03ad\7h\2\2\u03ad\u03ae\7a\2\2\u03ae\u03af\7{\2\2\u03af"+
		"\u03b0\7g\2\2\u03b0\u03b1\7c\2\2\u03b1\u03e6\7t\2\2\u03b2\u03b3\7j\2\2"+
		"\u03b3\u03b4\7q\2\2\u03b4\u03b5\7w\2\2\u03b5\u03e6\7t\2\2\u03b6\u03b7"+
		"\7o\2\2\u03b7\u03b8\7k\2\2\u03b8\u03b9\7p\2\2\u03b9\u03ba\7w\2\2\u03ba"+
		"\u03bb\7v\2\2\u03bb\u03e6\7g\2\2\u03bc\u03bd\7u\2\2\u03bd\u03be\7g\2\2"+
		"\u03be\u03bf\7e\2\2\u03bf\u03c0\7q\2\2\u03c0\u03c1\7p\2\2\u03c1\u03e6"+
		"\7f\2\2\u03c2\u03c3\7y\2\2\u03c3\u03c4\7g\2\2\u03c4\u03c5\7g\2\2\u03c5"+
		"\u03c6\7m\2\2\u03c6\u03c7\7a\2\2\u03c7\u03c8\7q\2\2\u03c8\u03c9\7h\2\2"+
		"\u03c9\u03ca\7a\2\2\u03ca\u03cb\7o\2\2\u03cb\u03cc\7q\2\2\u03cc\u03cd"+
		"\7p\2\2\u03cd\u03ce\7v\2\2\u03ce\u03e6\7j\2\2\u03cf\u03d0\7y\2\2\u03d0"+
		"\u03d1\7g\2\2\u03d1\u03d2\7g\2\2\u03d2\u03d3\7m\2\2\u03d3\u03d4\7a\2\2"+
		"\u03d4\u03d5\7q\2\2\u03d5\u03d6\7h\2\2\u03d6\u03d7\7a\2\2\u03d7\u03d8"+
		"\7{\2\2\u03d8\u03d9\7g\2\2\u03d9\u03da\7c\2\2\u03da\u03e6\7t\2\2\u03db"+
		"\u03dc\7s\2\2\u03dc\u03dd\7w\2\2\u03dd\u03de\7c\2\2\u03de\u03df\7t\2\2"+
		"\u03df\u03e0\7v\2\2\u03e0\u03e1\7g\2\2\u03e1\u03e6\7t\2\2\u03e2\u03e3"+
		"\7p\2\2\u03e3\u03e4\7q\2\2\u03e4\u03e6\7y\2\2\u03e5\u0089\3\2\2\2\u03e5"+
		"\u0093\3\2\2\2\u03e5\u009c\3\2\2\2\u03e5\u00a6\3\2\2\2\u03e5\u00b1\3\2"+
		"\2\2\u03e5\u00b9\3\2\2\2\u03e5\u00c2\3\2\2\2\u03e5\u00ce\3\2\2\2\u03e5"+
		"\u00d6\3\2\2\2\u03e5\u00df\3\2\2\2\u03e5\u00e8\3\2\2\2\u03e5\u00ec\3\2"+
		"\2\2\u03e5\u00ee\3\2\2\2\u03e5\u00f0\3\2\2\2\u03e5\u00f4\3\2\2\2\u03e5"+
		"\u00f8\3\2\2\2\u03e5\u00fd\3\2\2\2\u03e5\u0106\3\2\2\2\u03e5\u010c\3\2"+
		"\2\2\u03e5\u0112\3\2\2\2\u03e5\u0117\3\2\2\2\u03e5\u011e\3\2\2\2\u03e5"+
		"\u0127\3\2\2\2\u03e5\u012e\3\2\2\2\u03e5\u0134\3\2\2\2\u03e5\u0140\3\2"+
		"\2\2\u03e5\u014a\3\2\2\2\u03e5\u0152\3\2\2\2\u03e5\u015b\3\2\2\2\u03e5"+
		"\u0162\3\2\2\2\u03e5\u016c\3\2\2\2\u03e5\u0176\3\2\2\2\u03e5\u0180\3\2"+
		"\2\2\u03e5\u018a\3\2\2\2\u03e5\u019c\3\2\2\2\u03e5\u01a3\3\2\2\2\u03e5"+
		"\u01ac\3\2\2\2\u03e5\u01b8\3\2\2\2\u03e5\u01c4\3\2\2\2\u03e5\u01cf\3\2"+
		"\2\2\u03e5\u01d8\3\2\2\2\u03e5\u01e1\3\2\2\2\u03e5\u01ee\3\2\2\2\u03e5"+
		"\u01f2\3\2\2\2\u03e5\u01f7\3\2\2\2\u03e5\u0200\3\2\2\2\u03e5\u0209\3\2"+
		"\2\2\u03e5\u0211\3\2\2\2\u03e5\u021c\3\2\2\2\u03e5\u0229\3\2\2\2\u03e5"+
		"\u022f\3\2\2\2\u03e5\u023a\3\2\2\2\u03e5\u0247\3\2\2\2\u03e5\u0257\3\2"+
		"\2\2\u03e5\u0260\3\2\2\2\u03e5\u0266\3\2\2\2\u03e5\u026b\3\2\2\2\u03e5"+
		"\u0272\3\2\2\2\u03e5\u0277\3\2\2\2\u03e5\u0281\3\2\2\2\u03e5\u0288\3\2"+
		"\2\2\u03e5\u028b\3\2\2\2\u03e5\u028f\3\2\2\2\u03e5\u0294\3\2\2\2\u03e5"+
		"\u0298\3\2\2\2\u03e5\u029d\3\2\2\2\u03e5\u02a3\3\2\2\2\u03e5\u02a7\3\2"+
		"\2\2\u03e5\u02ab\3\2\2\2\u03e5\u02ad\3\2\2\2\u03e5\u02b2\3\2\2\2\u03e5"+
		"\u02b6\3\2\2\2\u03e5\u02ba\3\2\2\2\u03e5\u02be\3\2\2\2\u03e5\u02c1\3\2"+
		"\2\2\u03e5\u02c4\3\2\2\2\u03e5\u02c7\3\2\2\2\u03e5\u02cb\3\2\2\2\u03e5"+
		"\u02cf\3\2\2\2\u03e5\u02d3\3\2\2\2\u03e5\u02d7\3\2\2\2\u03e5\u02de\3\2"+
		"\2\2\u03e5\u02e5\3\2\2\2\u03e5\u02e8\3\2\2\2\u03e5\u02ed\3\2\2\2\u03e5"+
		"\u02f2\3\2\2\2\u03e5\u02f5\3\2\2\2\u03e5\u02fa\3\2\2\2\u03e5\u02ff\3\2"+
		"\2\2\u03e5\u0302\3\2\2\2\u03e5\u0306\3\2\2\2\u03e5\u030c\3\2\2\2\u03e5"+
		"\u0310\3\2\2\2\u03e5\u0313\3\2\2\2\u03e5\u0316\3\2\2\2\u03e5\u031b\3\2"+
		"\2\2\u03e5\u031e\3\2\2\2\u03e5\u0322\3\2\2\2\u03e5\u0325\3\2\2\2\u03e5"+
		"\u032b\3\2\2\2\u03e5\u0330\3\2\2\2\u03e5\u0334\3\2\2\2\u03e5\u0338\3\2"+
		"\2\2\u03e5\u033e\3\2\2\2\u03e5\u0344\3\2\2\2\u03e5\u0349\3\2\2\2\u03e5"+
		"\u034d\3\2\2\2\u03e5\u0351\3\2\2\2\u03e5\u0355\3\2\2\2\u03e5\u035c\3\2"+
		"\2\2\u03e5\u0364\3\2\2\2\u03e5\u036d\3\2\2\2\u03e5\u0376\3\2\2\2\u03e5"+
		"\u0379\3\2\2\2\u03e5\u037c\3\2\2\2\u03e5\u037f\3\2\2\2\u03e5\u0384\3\2"+
		"\2\2\u03e5\u0387\3\2\2\2\u03e5\u038b\3\2\2\2\u03e5\u0390\3\2\2\2\u03e5"+
		"\u039c\3\2\2\2\u03e5\u03a7\3\2\2\2\u03e5\u03b2\3\2\2\2\u03e5\u03b6\3\2"+
		"\2\2\u03e5\u03bc\3\2\2\2\u03e5\u03c2\3\2\2\2\u03e5\u03cf\3\2\2\2\u03e5"+
		"\u03db\3\2\2\2\u03e5\u03e2\3\2\2\2\u03e6\16\3\2\2\2\u03e7\u03e8\7h\2\2"+
		"\u03e8\u03e9\7t\2\2\u03e9\u03ea\7c\2\2\u03ea\u03eb\7i\2\2\u03eb\u03ec"+
		"\7o\2\2\u03ec\u03ed\7g\2\2\u03ed\u03ee\7p\2\2\u03ee\u03ef\7v\2\2\u03ef"+
		"\20\3\2\2\2\u03f0\u03f6\5\65\33\2\u03f1\u03f5\n\2\2\2\u03f2\u03f3\7^\2"+
		"\2\u03f3\u03f5\7)\2\2\u03f4\u03f1\3\2\2\2\u03f4\u03f2\3\2\2\2\u03f5\u03f8"+
		"\3\2\2\2\u03f6\u03f4\3\2\2\2\u03f6\u03f7\3\2\2\2\u03f7\u03f9\3\2\2\2\u03f8"+
		"\u03f6\3\2\2\2\u03f9\u03fa\5\65\33\2\u03fa\22\3\2\2\2\u03fb\u0401\5\67"+
		"\34\2\u03fc\u0400\n\3\2\2\u03fd\u03fe\7^\2\2\u03fe\u0400\7$\2\2\u03ff"+
		"\u03fc\3\2\2\2\u03ff\u03fd\3\2\2\2\u0400\u0403\3\2\2\2\u0401\u03ff\3\2"+
		"\2\2\u0401\u0402\3\2\2\2\u0402\u0404\3\2\2\2\u0403\u0401\3\2\2\2\u0404"+
		"\u0405\5\67\34\2\u0405\24\3\2\2\2\u0406\u0407\7v\2\2\u0407\u0408\7t\2"+
		"\2\u0408\u0409\7w\2\2\u0409\u0410\7g\2\2\u040a\u040b\7h\2\2\u040b\u040c"+
		"\7c\2\2\u040c\u040d\7n\2\2\u040d\u040e\7u\2\2\u040e\u0410\7g\2\2\u040f"+
		"\u0406\3\2\2\2\u040f\u040a\3\2\2\2\u0410\26\3\2\2\2\u0411\u0412\7p\2\2"+
		"\u0412\u0413\7w\2\2\u0413\u0414\7n\2\2\u0414\u0415\7n\2\2\u0415\30\3\2"+
		"\2\2\u0416\u0417\7?\2\2\u0417\u0418\7?\2\2\u0418\32\3\2\2\2\u0419\u041a"+
		"\7#\2\2\u041a\u041b\7?\2\2\u041b\34\3\2\2\2\u041c\u041d\7@\2\2\u041d\u041e"+
		"\7?\2\2\u041e\36\3\2\2\2\u041f\u0420\7>\2\2\u0420\u0421\7?\2\2\u0421 "+
		"\3\2\2\2\u0422\u0423\7@\2\2\u0423\"\3\2\2\2\u0424\u0425\7>\2\2\u0425$"+
		"\3\2\2\2\u0426\u0427\7(\2\2\u0427\u0428\7(\2\2\u0428&\3\2\2\2\u0429\u042a"+
		"\7~\2\2\u042a\u042b\7~\2\2\u042b(\3\2\2\2\u042c\u042d\7~\2\2\u042d*\3"+
		"\2\2\2\u042e\u042f\7#\2\2\u042f,\3\2\2\2\u0430\u0431\7*\2\2\u0431.\3\2"+
		"\2\2\u0432\u0433\7+\2\2\u0433\60\3\2\2\2\u0434\u0435\7}\2\2\u0435\62\3"+
		"\2\2\2\u0436\u0437\7\177\2\2\u0437\64\3\2\2\2\u0438\u0439\7)\2\2\u0439"+
		"\66\3\2\2\2\u043a\u043b\7$\2\2\u043b8\3\2\2\2\u043c\u043d\7\u0080\2\2"+
		"\u043d:\3\2\2\2\u043e\u043f\7]\2\2\u043f<\3\2\2\2\u0440\u0441\7_\2\2\u0441"+
		">\3\2\2\2\u0442\u0443\7%\2\2\u0443@\3\2\2\2\u0444\u0445\7.\2\2\u0445B"+
		"\3\2\2\2\u0446\u0447\7<\2\2\u0447D\3\2\2\2\u0448\u0449\7/\2\2\u0449F\3"+
		"\2\2\2\u044a\u044b\7\60\2\2\u044bH\3\2\2\2\u044c\u044d\7a\2\2\u044dJ\3"+
		"\2\2\2\u044e\u044f\7?\2\2\u044fL\3\2\2\2\u0450\u0451\7-\2\2\u0451N\3\2"+
		"\2\2\u0452\u0453\7A\2\2\u0453P\3\2\2\2\u0454\u0455\7,\2\2\u0455R\3\2\2"+
		"\2\u0456\u0457\7\61\2\2\u0457T\3\2\2\2\u0458\u0459\7\'\2\2\u0459V\3\2"+
		"\2\2\u045a\u045b\7&\2\2\u045bX\3\2\2\2\u045c\u0465\7\62\2\2\u045d\u0461"+
		"\t\4\2\2\u045e\u0460\t\5\2\2\u045f\u045e\3\2\2\2\u0460\u0463\3\2\2\2\u0461"+
		"\u045f\3\2\2\2\u0461\u0462\3\2\2\2\u0462\u0465\3\2\2\2\u0463\u0461\3\2"+
		"\2\2\u0464\u045c\3\2\2\2\u0464\u045d\3\2\2\2\u0465Z\3\2\2\2\u0466\u0468"+
		"\5G$\2\u0467\u0469\t\6\2\2\u0468\u0467\3\2\2\2\u0469\u046a\3\2\2\2\u046a"+
		"\u0468\3\2\2\2\u046a\u046b\3\2\2\2\u046b\\\3\2\2\2\u046c\u046e\t\7\2\2"+
		"\u046d\u046f\t\b\2\2\u046e\u046d\3\2\2\2\u046e\u046f\3\2\2\2\u046f\u0470"+
		"\3\2\2\2\u0470\u0471\5Y-\2\u0471^\3\2\2\2\u0472\u0473\t\t\2\2\u0473`\3"+
		"\2\2\2\u0474\u0475\t\n\2\2\u0475b\3\2\2\2\u0476\u047b\5_\60\2\u0477\u047b"+
		"\5I%\2\u0478\u047b\5a\61\2\u0479\u047b\5W,\2\u047a\u0476\3\2\2\2\u047a"+
		"\u0477\3\2\2\2\u047a\u0478\3\2\2\2\u047a\u0479\3\2\2\2\u047b\u047c\3\2"+
		"\2\2\u047c\u047a\3\2\2\2\u047c\u047d\3\2\2\2\u047d\u0487\3\2\2\2\u047e"+
		"\u0486\5_\60\2\u047f\u0486\5W,\2\u0480\u0486\5a\61\2\u0481\u0486\t\6\2"+
		"\2\u0482\u0486\5C\"\2\u0483\u0486\5E#\2\u0484\u0486\5I%\2\u0485\u047e"+
		"\3\2\2\2\u0485\u047f\3\2\2\2\u0485\u0480\3\2\2\2\u0485\u0481\3\2\2\2\u0485"+
		"\u0482\3\2\2\2\u0485\u0483\3\2\2\2\u0485\u0484\3\2\2\2\u0486\u0489\3\2"+
		"\2\2\u0487\u0485\3\2\2\2\u0487\u0488\3\2\2\2\u0488d\3\2\2\2\u0489\u0487"+
		"\3\2\2\2\u048a\u048b\7\61\2\2\u048b\u048c\7\61\2\2\u048c\u0490\3\2\2\2"+
		"\u048d\u048f\n\13\2\2\u048e\u048d\3\2\2\2\u048f\u0492\3\2\2\2\u0490\u048e"+
		"\3\2\2\2\u0490\u0491\3\2\2\2\u0491\u0494\3\2\2\2\u0492\u0490\3\2\2\2\u0493"+
		"\u0495\7\17\2\2\u0494\u0493\3\2\2\2\u0494\u0495\3\2\2\2\u0495\u0497\3"+
		"\2\2\2\u0496\u0498\t\f\2\2\u0497\u0496\3\2\2\2\u0498\u04a5\3\2\2\2\u0499"+
		"\u049a\7\61\2\2\u049a\u049b\7,\2\2\u049b\u049f\3\2\2\2\u049c\u049e\13"+
		"\2\2\2\u049d\u049c\3\2\2\2\u049e\u04a1\3\2\2\2\u049f\u04a0\3\2\2\2\u049f"+
		"\u049d\3\2\2\2\u04a0\u04a2\3\2\2\2\u04a1\u049f\3\2\2\2\u04a2\u04a3\7,"+
		"\2\2\u04a3\u04a5\7\61\2\2\u04a4\u048a\3\2\2\2\u04a4\u0499\3\2\2\2\u04a5"+
		"\u04a6\3\2\2\2\u04a6\u04a7\b\63\2\2\u04a7f\3\2\2\2\u04a8\u04aa\t\r\2\2"+
		"\u04a9\u04a8\3\2\2\2\u04aa\u04ab\3\2\2\2\u04ab\u04a9\3\2\2\2\u04ab\u04ac"+
		"\3\2\2\2\u04ac\u04ad\3\2\2\2\u04ad\u04ae\b\64\2\2\u04aeh\3\2\2\2\u04af"+
		"\u04b0\13\2\2\2\u04b0j\3\2\2\2\27\2\u03e5\u03f4\u03f6\u03ff\u0401\u040f"+
		"\u0461\u0464\u046a\u046e\u047a\u047c\u0485\u0487\u0490\u0494\u0497\u049f"+
		"\u04a4\u04ab\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}