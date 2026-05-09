package com.asiaproxy.trader.engine;

import com.asiaproxy.trader.model.Catalyst;
import com.asiaproxy.trader.model.MarketSession;
import com.asiaproxy.trader.model.Pair;
import com.asiaproxy.trader.model.QuickLink;

import java.util.Arrays;
import java.util.List;

public final class Catalog {
    private Catalog() {}

    public static List<MarketSession> sessions() {
        return Arrays.asList(
            new MarketSession("KOSPI + Nikkei", "JP/KR", 7 * 60, 13 * 60 + 30, "A",
                new String[]{"000660.KS (SK Hynix)", "005930.KS (Samsung)", "8035.T (Tokyo Electron)", "7203.T (Toyota)"},
                new String[]{"NVDA", "MU", "AAPL", "TSLA"}),

            new MarketSession("TWSE Taiwan", "TW", 8 * 60, 12 * 60 + 30, "A",
                new String[]{"2330.TW (TSMC)", "2317.TW (Foxconn)"},
                new String[]{"NVDA", "AAPL", "AMD"}),

            new MarketSession("HKEX + China", "HK/CN", 8 * 60 + 30, 15 * 60, "A",
                new String[]{"9988.HK (Alibaba)", "1211.HK (BYD)", "1128.HK (Wynn Macau)", "1928.HK (Sands China)", "9987.HK (Yum China)"},
                new String[]{"BABA", "TSLA", "WYNN", "LVS", "YUMC"}),

            new MarketSession("India NSE", "IN", 10 * 60 + 45, 16 * 60, "C",
                new String[]{"INFY (Infosys)", "HDB (HDFC Bank)"},
                new String[]{"INFY", "HDB"}),

            new MarketSession("US Pre-Market", "US", 15 * 60, 20 * 60 + 30, "A",
                new String[]{"ES (S&P fut)", "NQ (Nasdaq fut)", "VIX"},
                new String[]{"NVDA", "TSLA", "BABA", "AAPL", "AMD"}),

            new MarketSession("US Regular Hours", "US", 20 * 60 + 30, 23 * 60 + 59, "A",
                new String[]{"ES", "NQ"},
                new String[]{"NVDA", "TSLA", "BABA", "AAPL", "MU", "WYNN", "LVS"})
        );
    }

    public static List<Pair> pairs() {
        return Arrays.asList(
            new Pair("nvda_tsmc", "Semiconductor", "NVDA", "NVIDIA",
                "2330.TW", "TSMC", 5,
                "TSMC Monthly Revenue (10th of month, ~14:00 ICT)",
                "https://www.tradingview.com/symbols/NASDAQ-NVDA/",
                "https://www.tradingview.com/symbols/TWSE-2330/",
                new String[]{
                    "Wait 15-30 min after Asia close to confirm reaction",
                    "Check ES / NQ futures direction",
                    "Beat > 5% + futures green = strong setup",
                    "Risk ≤ 0.5–1% per trade"
                }),

            new Pair("aapl_foxconn", "Semiconductor", "AAPL", "Apple",
                "2317.TW", "Foxconn (Hon Hai)", 4,
                "Foxconn Monthly Revenue (5th-10th)",
                "https://www.tradingview.com/symbols/NASDAQ-AAPL/",
                "https://www.tradingview.com/symbols/TWSE-2317/",
                new String[]{
                    "Foxconn often leads AAPL by 1 session",
                    "Watch iPhone seasonality (Q3/Q4 ramp)",
                    "Cross-check with Pegatron (4938.TW)"
                }),

            new Pair("nvda_skhynix", "Semiconductor", "NVDA", "NVIDIA",
                "000660.KS", "SK Hynix", 4,
                "HBM demand cycle, SK Hynix earnings",
                "https://www.tradingview.com/symbols/NASDAQ-NVDA/",
                "https://www.tradingview.com/symbols/KRX-000660/",
                new String[]{
                    "HBM3E supply = NVDA Blackwell demand proxy",
                    "Korea opens 07:00 ICT — earliest signal of the day",
                    "Watch DRAM contract pricing"
                }),

            new Pair("mu_skhynix", "Semiconductor", "MU", "Micron",
                "000660.KS", "SK Hynix", 4,
                "Memory cycle (DRAM/NAND pricing)",
                "https://www.tradingview.com/symbols/NASDAQ-MU/",
                "https://www.tradingview.com/symbols/KRX-000660/",
                new String[]{
                    "MU and SK Hynix correlate > 0.8 historically",
                    "Watch Samsung 005930 too",
                    "Memory contract prices monthly"
                }),

            new Pair("tsla_byd", "EV", "TSLA", "Tesla",
                "1211.HK", "BYD", 5,
                "BYD Monthly EV Sales (1st-3rd)",
                "https://www.tradingview.com/symbols/NASDAQ-TSLA/",
                "https://www.tradingview.com/symbols/HKEX-1211/",
                new String[]{
                    "BYD sales are released first business day of the month",
                    "Beat 10%+ usually moves TSLA premkt",
                    "Watch China EV sub-sector breadth"
                }),

            new Pair("baba_baba", "China ADR", "BABA", "Alibaba",
                "9988.HK", "Alibaba HK", 5,
                "Hong Kong session sets ADR tone",
                "https://www.tradingview.com/symbols/NYSE-BABA/",
                "https://www.tradingview.com/symbols/HKEX-9988/",
                new String[]{
                    "ADR ↔ HK arbitrage usually < 1%",
                    "China stimulus headlines move both",
                    "Friday HK close → Monday ADR gap"
                }),

            new Pair("wynn_wynnmacau", "Gaming", "WYNN", "Wynn Resorts",
                "1128.HK", "Wynn Macau", 5,
                "Macau monthly GGR (1st-2nd) + DICJ data",
                "https://www.tradingview.com/symbols/NASDAQ-WYNN/",
                "https://www.tradingview.com/symbols/HKEX-1128/",
                new String[]{
                    "DICJ releases GGR on the 1st",
                    "GGR YoY > +10% = bullish for WYNN/LVS",
                    "Holiday windows: CNY, Golden Week"
                }),

            new Pair("lvs_sands", "Gaming", "LVS", "Las Vegas Sands",
                "1928.HK", "Sands China", 4,
                "Macau GGR + Sands China premium",
                "https://www.tradingview.com/symbols/NYSE-LVS/",
                "https://www.tradingview.com/symbols/HKEX-1928/",
                new String[]{
                    "LVS has zero US casinos — pure Macau/SG bet",
                    "Marina Bay Sands SG separate driver"
                }),

            new Pair("yumc_yumchina", "China Consumer", "YUMC", "Yum China",
                "9987.HK", "Yum China HK", 4,
                "Quarterly same-store sales (KFC / Pizza Hut China) + HK listing",
                "https://www.tradingview.com/symbols/NYSE-YUMC/",
                "https://www.tradingview.com/symbols/HKEX-9987/",
                new String[]{
                    "Dual-listed — HK premium / discount usually < 2%",
                    "Same-store sales = consumer-recovery proxy",
                    "Holiday windows (CNY, Golden Week) move SSS most"
                }),

            new Pair("jd_jd", "China ADR", "JD", "JD.com",
                "9618.HK", "JD HK", 3,
                "HK session sets ADR direction",
                "https://www.tradingview.com/symbols/NASDAQ-JD/",
                "https://www.tradingview.com/symbols/HKEX-9618/",
                new String[]{
                    "Watch BABA as broader China e-commerce proxy"
                }),

            new Pair("bidu_bidu", "China ADR", "BIDU", "Baidu",
                "9888.HK", "Baidu HK", 3,
                "HK ADR arbitrage",
                "https://www.tradingview.com/symbols/NASDAQ-BIDU/",
                "https://www.tradingview.com/symbols/HKEX-9888/",
                new String[]{
                    "AI / autonomous narrative drives both",
                    "Watch TCEHY for China tech breadth"
                })
        );
    }

    public static List<Catalyst> catalysts() {
        return Arrays.asList(
            new Catalyst("Macau Monthly GGR (DICJ)", "1st – 2nd", "WYNN, LVS, MLCO", 1, 2,
                "https://www.dicj.gov.mo/web/en/information/DadosEstat_mensal/index.html"),
            new Catalyst("BYD Monthly EV Sales", "1st – 3rd", "TSLA, NIO, LI, XPEV", 1, 3,
                "https://www.byd.com/en/news"),
            new Catalyst("Foxconn Monthly Revenue", "5th – 10th", "AAPL",  5, 10,
                "https://www.foxconn.com/en-us/investor/monthly-revenue"),
            new Catalyst("TSMC Monthly Revenue", "10th", "NVDA, AAPL, AMD", 10, 10,
                "https://www.tsmc.com/english/investor-relations/monthly-revenue"),
            new Catalyst("China CPI / PPI", "9th – 12th", "Broad China", 9, 12,
                "https://th.investing.com/economic-calendar/chinese-cpi-743"),
            new Catalyst("China NBS / Caixin PMI", "End / 1st", "FXI, KWEB, broad", 28, 31,
                "https://th.investing.com/economic-calendar/chinese-manufacturing-pmi-594"),
            new Catalyst("BOJ Rate Decision", "Mid-month", "DXY, JPY pairs", 14, 20,
                "https://th.investing.com/central-banks/boj"),
            new Catalyst("PBOC LPR", "20th", "China stocks", 20, 20,
                "https://th.investing.com/central-banks/peoples-bank-of-china")
        );
    }

    public static List<QuickLink> globalLinks() {
        return Arrays.asList(
            new QuickLink("Investing.com Calendar", "https://th.investing.com/economic-calendar"),
            new QuickLink("TradingView Markets", "https://www.tradingview.com/markets/"),
            new QuickLink("Benzinga Pro Movers", "https://pro.benzinga.com/movers"),
            new QuickLink("Yahoo Finance — Pre-market", "https://finance.yahoo.com/screener/predefined/aggressive_small_caps"),
            new QuickLink("DICJ Macau GGR", "https://www.dicj.gov.mo/web/en/information/DadosEstat_mensal/index.html"),
            new QuickLink("TSMC Investor Relations", "https://www.tsmc.com/english/investor-relations/monthly-revenue"),
            new QuickLink("Foxconn Investor Relations", "https://www.foxconn.com/en-us/investor/monthly-revenue"),
            new QuickLink("BYD News", "https://www.byd.com/en/news"),
            new QuickLink("SCMP — Business", "https://www.scmp.com/business"),
            new QuickLink("Reuters — Asia Markets", "https://www.reuters.com/markets/asia/"),
            new QuickLink("Caixin Global", "https://www.caixinglobal.com/"),
            new QuickLink("X — @DeItaone", "https://x.com/deitaone"),
            new QuickLink("X — @FirstSquawk", "https://x.com/firstsquawk"),
            new QuickLink("X — @zerohedge", "https://x.com/zerohedge"),
            new QuickLink("X — @BloombergAsia", "https://x.com/bloombergasia")
        );
    }
}
