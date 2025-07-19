import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * The Transaction class holds data for a single trade.
 */
class Transaction
{
    String type;
    String symbol;
    int quantity;
    double price;
    LocalDateTime timestamp;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    Transaction(String type, String symbol, int quantity, double price)
    {
        this.type = type;
        this.symbol = symbol;
        this.quantity = quantity;
        this.price = price;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString()
    {
        return String.format("[%s] %-4s %d %s @ \u20B9%.2f",
                dtf.format(timestamp), type, quantity, symbol, price);
    }
}

class Stock
{
    String symbol;
    String name;
    double price;

    Stock(String symbol, String name, double price)
    {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
    }

    public String toString()
    {
        return symbol + " - " + name + " @ \u20B9" + price;
    }
}

class User
{
    String name;
    double balance;
    Map<String, Integer> portfolio;
    ArrayList<Transaction> transactionHistory;

    User(String name, double balance)
    {
        this.name = name;
        this.balance = balance;
        this.portfolio = new HashMap<>();
        this.transactionHistory = new ArrayList<>();
    }

    public boolean buyStock(Stock stock, int quantity)
    {
        double cost = stock.price * quantity;
        if (cost > balance)
            return false;
        balance -= cost;
        portfolio.put(stock.symbol, portfolio.getOrDefault(stock.symbol, 0) + quantity);
        transactionHistory.add(new Transaction("BUY", stock.symbol, quantity, stock.price));
        return true;
    }

    public boolean sellStock(Stock stock, int quantity)
    {
        int owned = portfolio.getOrDefault(stock.symbol, 0);
        if (quantity > owned)
            return false;
        portfolio.put(stock.symbol, owned - quantity);
        balance += stock.price * quantity;
        transactionHistory.add(new Transaction("SELL", stock.symbol, quantity, stock.price));
        return true;
    }

    public String getPortfolioString(Map<String, Stock> stockMap)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio of ").append(name).append(":\n\n");
        double totalValue = 0;
        for (String sym : portfolio.keySet())
        {
            int qty = portfolio.get(sym);
            if (qty == 0)
                continue;
            
            Stock s = stockMap.get(sym);
            double value = qty * s.price;
            sb.append(String.format("%s: %d shares @ \u20B9%.2f = \u20B9%.2f\n", sym, qty, s.price, value));
            totalValue += value;
        }
        sb.append(String.format("\nCash Balance: \u20B9%.2f", balance));
        sb.append(String.format("\nTotal Portfolio Value: \u20B9%.2f", (balance + totalValue)));
        return sb.toString();
    }
}

public class StockTradingGUI
{
    // UI Components
    private JFrame frame;
    private JPanel mainContainer;
    private CardLayout cardLayout;
    private JTextArea output;
    private JComboBox<String> stockDropdown;
    private JTextField quantityField;

    // Data
    private Map<String, Stock> stockMap = new HashMap<>();
    private User user;

    // Panel Identifiers
    private static final String LOGIN_PANEL = "LoginPanel";
    private static final String TRADING_PANEL = "TradingPanel";

    public void main()
    {
        addStocks();

        // --- Main Frame Setup ---
        frame = new JFrame("Stock Trading Platform");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(650, 550);

        // --- CardLayout Container ---
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        // --- Create and add panels ---
        JPanel loginPanel = createLoginPanel();
        JPanel tradingPanel = createTradingPanel();

        mainContainer.add(loginPanel, LOGIN_PANEL);
        mainContainer.add(tradingPanel, TRADING_PANEL);

        frame.add(mainContainer);
        frame.setLocationRelativeTo(null); // Center the frame
        frame.setVisible(true);

        cardLayout.show(mainContainer, LOGIN_PANEL); // Show login panel first
    }

    /**
     * Creates the initial setup panel for user name and balance input.
     */
    private JPanel createLoginPanel()
    {
        // --- Styling ---
        Color darkBackground = new Color(30, 30, 30);
        Color greenText = new Color(40, 200, 40);
        Font labelFont = new Font("Arial", Font.BOLD, 16);
        Font fieldFont = new Font("Consolas", Font.PLAIN, 16);

        // --- Panel Setup ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(darkBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Components ---
        JLabel titleLabel = new JLabel("Welcome to the Trading Platform");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(greenText);

        JLabel nameLabel = new JLabel("Your Name:");
        nameLabel.setFont(labelFont);
        nameLabel.setForeground(greenText);
        JTextField nameField = new JTextField(15);
        nameField.setFont(fieldFont);

        JLabel balanceLabel = new JLabel("Starting Balance (\u20B9):");
        balanceLabel.setFont(labelFont);
        balanceLabel.setForeground(greenText);
        JTextField balanceField = new JTextField(15);
        balanceField.setFont(fieldFont);

        JButton startButton = new JButton("Start Trading");
        startButton.setFont(labelFont);

        JLabel errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.RED);
        errorLabel.setHorizontalAlignment(SwingConstants.CENTER);


        // --- Layout ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(nameLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(balanceLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(balanceField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(startButton, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        panel.add(errorLabel, gbc);


        // --- Action Listener ---
        startButton.addActionListener(e ->{
            String name = nameField.getText().trim();
            String balanceStr = balanceField.getText().trim();

            if (name.isEmpty() || balanceStr.isEmpty()) {
                errorLabel.setText("Name and balance cannot be empty.");
                return;
            }

            try
            {
                double balance = Double.parseDouble(balanceStr);
                if (balance < 0)
                {
                     errorLabel.setText("Balance must be a positive number.");
                    return;
                }
                // --- On Success ---
                user = new User(name, balance);
                frame.setTitle("Stock Trading Platform - " + user.name);
                showMarket(); // Pre-load market data in the output
                cardLayout.show(mainContainer, TRADING_PANEL); // Switch to trading panel
            }
            catch (NumberFormatException ex)
            {
                errorLabel.setText("Please enter a valid number for the balance.");
            }
        });

        return panel;
    }

    /**
     * Creates the main trading interface panel.
     */
    private JPanel createTradingPanel()
    {
        // --- UI Styling ---
        Color darkBackground = new Color(30, 30, 30);
        Color componentBg = new Color(50, 50, 50);
        Color greenText = new Color(40, 200, 40);
        Font mainFont = new Font("Consolas", Font.PLAIN, 14);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(darkBackground);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Input Controls ---
        JLabel stockLabel = new JLabel("Select Stock:");
        stockLabel.setForeground(greenText);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockDropdown = new JComboBox<>();
        for (String symbol : stockMap.keySet())
            stockDropdown.addItem(symbol);
        
        stockDropdown.setFont(mainFont);
        stockDropdown.setMaximumSize(new Dimension(200, 30));
        stockDropdown.setForeground(greenText);
        stockDropdown.setBackground(componentBg);

        JLabel quantityLabel = new JLabel("Enter Quantity:");
        quantityLabel.setForeground(greenText);
        quantityLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        quantityField = new JTextField(5);
        quantityField.setMaximumSize(new Dimension(200, 30));
        quantityField.setFont(mainFont);
        quantityField.setForeground(greenText);
        quantityField.setBackground(componentBg);
        quantityField.setCaretColor(Color.WHITE);

        // --- Action Buttons ---
        JButton buyButton = new JButton("Buy");
        JButton sellButton = new JButton("Sell");
        JButton viewMarketButton = new JButton("View Market");
        JButton portfolioButton = new JButton("View Portfolio");
        JButton transactionsButton = new JButton("Transactions");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(darkBackground);
        JButton[] buttons = {buyButton, sellButton, viewMarketButton, portfolioButton, transactionsButton};
        for (JButton b : buttons)
        {
            b.setFont(mainFont);
            b.setBackground(componentBg);
            b.setForeground(greenText);
            b.setFocusPainted(false);
            buttonPanel.add(b);
        }

        // --- Output Area ---
        output = new JTextArea(12, 35);
        output.setEditable(false);
        output.setBackground(new Color(20, 20, 20));
        output.setForeground(greenText);
        output.setFont(mainFont);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(output);

        // --- Add components to main panel ---
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(stockLabel);
        panel.add(stockDropdown);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(quantityLabel);
        panel.add(quantityField);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(scroll);

        // --- Action Listeners ---
        buyButton.addActionListener(e -> handleBuy());
        sellButton.addActionListener(e -> handleSell());
        viewMarketButton.addActionListener(e -> showMarket());
        portfolioButton.addActionListener(e -> showPortfolio());
        transactionsButton.addActionListener(e -> showTransactions());

        return panel;
    }

    private void addStocks()
    {
        stockMap.put("TCS", new Stock("TCS", "Tata Consultancy Services", 3700.0));
        stockMap.put("INFY", new Stock("INFY", "Infosys", 1500.0));
        stockMap.put("RELI", new Stock("RELI", "Reliance Industries", 2500.0));
        stockMap.put("HDFC", new Stock("HDFC", "HDFC Bank", 1600.0));
    }

    private void handleBuy()
    {
        String symbol = (String) stockDropdown.getSelectedItem();
        int qty = parseQuantity();
        if (qty <= 0)
        {
            show("Invalid quantity. Please enter a positive number.");
            return;
        }
        if (user.buyStock(stockMap.get(symbol), qty))
        {
            show("Successfully bought " + qty + " shares of " + symbol + ".\n\n" + user.getPortfolioString(stockMap));
        }
        else
        {
            show("Not enough balance to buy " + qty + " shares of " + symbol + ".");
        }
    }

    private void handleSell()
    {
        String symbol = (String) stockDropdown.getSelectedItem();
        int qty = parseQuantity();
        if (qty <= 0)
        {
            show("Invalid quantity. Please enter a positive number.");
            return;
        }
        if (user.sellStock(stockMap.get(symbol), qty))
        {
            show("Successfully sold " + qty + " shares of " + symbol + ".\n\n" + user.getPortfolioString(stockMap));
        }
        else
        {
            show("You do not own enough shares of " + symbol + " to sell.");
        }
    }

    private void showMarket()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Market Data:\n\n");
        for (Stock s : stockMap.values())
        {
            sb.append(s).append("\n");
        }
        show(sb.toString());
    }

    private void showPortfolio()
    {
        show(user.getPortfolioString(stockMap));
    }

    private void showTransactions()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction History for ").append(user.name).append(":\n\n");
        if (user.transactionHistory.isEmpty())
        {
            sb.append("No transactions have been made yet.");
        }
        else
        {
            for (int i = user.transactionHistory.size() - 1; i >= 0; i--)
            {
                sb.append(user.transactionHistory.get(i).toString()).append("\n");
            }
        }
        show(sb.toString());
    }

    private void show(String text)
    {
        if(output != null)
        {
            output.setText(text);
            output.setCaretPosition(0);
        }
    }

    private int parseQuantity()
    {
        try
        {
            return Integer.parseInt(quantityField.getText());
        }
        catch (NumberFormatException e)
        {
            return -1;
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new StockTradingGUI().main());
    }
}
