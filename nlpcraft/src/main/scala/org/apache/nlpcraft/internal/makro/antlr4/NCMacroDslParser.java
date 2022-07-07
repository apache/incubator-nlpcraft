// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/makro/antlr4/NCMacroDsl.g4 by ANTLR 4.10.1
package org.apache.nlpcraft.internal.makro.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCMacroDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.10.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		LCURLY=1, RCURLY=2, VERT=3, COMMA=4, UNDERSCORE=5, LBR=6, RBR=7, QUESTION=8, 
		REGEX_TXT=9, IDL_TXT=10, TXT=11, MINMAX=12, WS=13, ERR_CHAR=14;
	public static final int
		RULE_makro = 0, RULE_expr = 1, RULE_item = 2, RULE_syn = 3, RULE_group = 4, 
		RULE_list = 5, RULE_minMax = 6, RULE_minMaxShortcut = 7;
	private static String[] makeRuleNames() {
		return new String[] {
			"makro", "expr", "item", "syn", "group", "list", "minMax", "minMaxShortcut"
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

	@Override
	public String getGrammarFileName() { return "NCMacroDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCMacroDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class MakroContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(NCMacroDslParser.EOF, 0); }
		public MakroContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_makro; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterMakro(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitMakro(this);
		}
	}

	public final MakroContext makro() throws RecognitionException {
		MakroContext _localctx = new MakroContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_makro);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			expr(0);
			setState(17);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_expr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(20);
			item();
			}
			_ctx.stop = _input.LT(-1);
			setState(26);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(22);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(23);
					item();
					}
					} 
				}
				setState(28);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class ItemContext extends ParserRuleContext {
		public SynContext syn() {
			return getRuleContext(SynContext.class,0);
		}
		public GroupContext group() {
			return getRuleContext(GroupContext.class,0);
		}
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitItem(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		ItemContext _localctx = new ItemContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_item);
		try {
			setState(31);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case REGEX_TXT:
			case IDL_TXT:
			case TXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(29);
				syn();
				}
				break;
			case LCURLY:
				enterOuterAlt(_localctx, 2);
				{
				setState(30);
				group();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SynContext extends ParserRuleContext {
		public TerminalNode TXT() { return getToken(NCMacroDslParser.TXT, 0); }
		public TerminalNode REGEX_TXT() { return getToken(NCMacroDslParser.REGEX_TXT, 0); }
		public TerminalNode IDL_TXT() { return getToken(NCMacroDslParser.IDL_TXT, 0); }
		public SynContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_syn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterSyn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitSyn(this);
		}
	}

	public final SynContext syn() throws RecognitionException {
		SynContext _localctx = new SynContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_syn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << REGEX_TXT) | (1L << IDL_TXT) | (1L << TXT))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class GroupContext extends ParserRuleContext {
		public TerminalNode LCURLY() { return getToken(NCMacroDslParser.LCURLY, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public TerminalNode RCURLY() { return getToken(NCMacroDslParser.RCURLY, 0); }
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public GroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_group; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitGroup(this);
		}
	}

	public final GroupContext group() throws RecognitionException {
		GroupContext _localctx = new GroupContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_group);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(35);
			match(LCURLY);
			setState(36);
			list(0);
			setState(37);
			match(RCURLY);
			setState(39);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(38);
				minMax();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ListContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode UNDERSCORE() { return getToken(NCMacroDslParser.UNDERSCORE, 0); }
		public TerminalNode VERT() { return getToken(NCMacroDslParser.VERT, 0); }
		public ListContext list() {
			return getRuleContext(ListContext.class,0);
		}
		public ListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_list; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitList(this);
		}
	}

	public final ListContext list() throws RecognitionException {
		return list(0);
	}

	private ListContext list(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ListContext _localctx = new ListContext(_ctx, _parentState);
		ListContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_list, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLY:
			case REGEX_TXT:
			case IDL_TXT:
			case TXT:
				{
				setState(42);
				expr(0);
				}
				break;
			case UNDERSCORE:
				{
				setState(43);
				match(UNDERSCORE);
				setState(44);
				match(VERT);
				setState(45);
				list(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(56);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(54);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
					case 1:
						{
						_localctx = new ListContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_list);
						setState(48);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(49);
						match(VERT);
						setState(50);
						expr(0);
						}
						break;
					case 2:
						{
						_localctx = new ListContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_list);
						setState(51);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(52);
						match(VERT);
						setState(53);
						match(UNDERSCORE);
						}
						break;
					}
					} 
				}
				setState(58);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class MinMaxContext extends ParserRuleContext {
		public MinMaxShortcutContext minMaxShortcut() {
			return getRuleContext(MinMaxShortcutContext.class,0);
		}
		public TerminalNode MINMAX() { return getToken(NCMacroDslParser.MINMAX, 0); }
		public MinMaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMax; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_minMax);
		try {
			setState(61);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case QUESTION:
				enterOuterAlt(_localctx, 1);
				{
				setState(59);
				minMaxShortcut();
				}
				break;
			case MINMAX:
				enterOuterAlt(_localctx, 2);
				{
				setState(60);
				match(MINMAX);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MinMaxShortcutContext extends ParserRuleContext {
		public TerminalNode QUESTION() { return getToken(NCMacroDslParser.QUESTION, 0); }
		public MinMaxShortcutContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxShortcut; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).enterMinMaxShortcut(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCMacroDslListener ) ((NCMacroDslListener)listener).exitMinMaxShortcut(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_minMaxShortcut);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			match(QUESTION);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 5:
			return list_sempred((ListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean list_sempred(ListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001\u000eB\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0005\u0001\u0019\b\u0001\n\u0001\f\u0001\u001c\t\u0001"+
		"\u0001\u0002\u0001\u0002\u0003\u0002 \b\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004(\b\u0004"+
		"\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0003\u0005"+
		"/\b\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0005\u00057\b\u0005\n\u0005\f\u0005:\t\u0005\u0001\u0006"+
		"\u0001\u0006\u0003\u0006>\b\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0000\u0002\u0002\n\b\u0000\u0002\u0004\u0006\b\n\f\u000e\u0000\u0001"+
		"\u0001\u0000\t\u000b@\u0000\u0010\u0001\u0000\u0000\u0000\u0002\u0013"+
		"\u0001\u0000\u0000\u0000\u0004\u001f\u0001\u0000\u0000\u0000\u0006!\u0001"+
		"\u0000\u0000\u0000\b#\u0001\u0000\u0000\u0000\n.\u0001\u0000\u0000\u0000"+
		"\f=\u0001\u0000\u0000\u0000\u000e?\u0001\u0000\u0000\u0000\u0010\u0011"+
		"\u0003\u0002\u0001\u0000\u0011\u0012\u0005\u0000\u0000\u0001\u0012\u0001"+
		"\u0001\u0000\u0000\u0000\u0013\u0014\u0006\u0001\uffff\uffff\u0000\u0014"+
		"\u0015\u0003\u0004\u0002\u0000\u0015\u001a\u0001\u0000\u0000\u0000\u0016"+
		"\u0017\n\u0001\u0000\u0000\u0017\u0019\u0003\u0004\u0002\u0000\u0018\u0016"+
		"\u0001\u0000\u0000\u0000\u0019\u001c\u0001\u0000\u0000\u0000\u001a\u0018"+
		"\u0001\u0000\u0000\u0000\u001a\u001b\u0001\u0000\u0000\u0000\u001b\u0003"+
		"\u0001\u0000\u0000\u0000\u001c\u001a\u0001\u0000\u0000\u0000\u001d \u0003"+
		"\u0006\u0003\u0000\u001e \u0003\b\u0004\u0000\u001f\u001d\u0001\u0000"+
		"\u0000\u0000\u001f\u001e\u0001\u0000\u0000\u0000 \u0005\u0001\u0000\u0000"+
		"\u0000!\"\u0007\u0000\u0000\u0000\"\u0007\u0001\u0000\u0000\u0000#$\u0005"+
		"\u0001\u0000\u0000$%\u0003\n\u0005\u0000%\'\u0005\u0002\u0000\u0000&("+
		"\u0003\f\u0006\u0000\'&\u0001\u0000\u0000\u0000\'(\u0001\u0000\u0000\u0000"+
		"(\t\u0001\u0000\u0000\u0000)*\u0006\u0005\uffff\uffff\u0000*/\u0003\u0002"+
		"\u0001\u0000+,\u0005\u0005\u0000\u0000,-\u0005\u0003\u0000\u0000-/\u0003"+
		"\n\u0005\u0001.)\u0001\u0000\u0000\u0000.+\u0001\u0000\u0000\u0000/8\u0001"+
		"\u0000\u0000\u000001\n\u0003\u0000\u000012\u0005\u0003\u0000\u000027\u0003"+
		"\u0002\u0001\u000034\n\u0002\u0000\u000045\u0005\u0003\u0000\u000057\u0005"+
		"\u0005\u0000\u000060\u0001\u0000\u0000\u000063\u0001\u0000\u0000\u0000"+
		"7:\u0001\u0000\u0000\u000086\u0001\u0000\u0000\u000089\u0001\u0000\u0000"+
		"\u00009\u000b\u0001\u0000\u0000\u0000:8\u0001\u0000\u0000\u0000;>\u0003"+
		"\u000e\u0007\u0000<>\u0005\f\u0000\u0000=;\u0001\u0000\u0000\u0000=<\u0001"+
		"\u0000\u0000\u0000>\r\u0001\u0000\u0000\u0000?@\u0005\b\u0000\u0000@\u000f"+
		"\u0001\u0000\u0000\u0000\u0007\u001a\u001f\'.68=";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}