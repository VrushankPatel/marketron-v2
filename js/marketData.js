/**
 * @class MarketData
 * @description Handles market data simulation and chart rendering
 * @author Vrushank Patel
 */
class MarketData {
    /**
     * @constructor
     * @author Vrushank Patel
     */
    constructor() {
        this.symbols = ['AAPL', 'GOOGL', 'MSFT', 'AMZN'];
        this.prices = {};
        this.priceHistory = {};
        this.canvas = document.getElementById('priceChart');
        this.ctx = this.canvas.getContext('2d');
        this.selectedSymbol = this.symbols[0];
        this.timeframe = 15;
        this.initializePrices();
        this.startSimulation();
        this.initializeChart();
    }

    /**
     * @method initializePrices
     * @description Initializes random prices for all symbols
     * @author Vrushank Patel
     */
    initializePrices() {
        this.symbols.forEach(symbol => {
            this.prices[symbol] = {
                price: Math.random() * 1000 + 100,
                change: 0
            };
            this.priceHistory[symbol] = [];
        });
    }

    /**
     * @method initializeChart
     * @description Sets up the chart UI and event listeners
     * @author Vrushank Patel
     */
    initializeChart() {
        const selector = document.getElementById('chartSymbol');
        this.symbols.forEach(symbol => {
            const option = document.createElement('option');
            option.value = option.textContent = symbol;
            selector.appendChild(option);
        });

        selector.addEventListener('change', (e) => {
            this.selectedSymbol = e.target.value;
            this.renderChart();
        });

        document.querySelectorAll('.time-button').forEach(button => {
            button.addEventListener('click', (e) => {
                this.timeframe = parseInt(e.target.dataset.time);
                document.querySelectorAll('.time-button').forEach(b => b.classList.remove('active'));
                e.target.classList.add('active');
                this.renderChart();
            });
        });

        this.resizeCanvas();
        window.addEventListener('resize', () => this.resizeCanvas());
    }

    /**
     * @method resizeCanvas
     * @description Adjusts canvas size based on container dimensions
     * @author Vrushank Patel
     */
    resizeCanvas() {
        const rect = this.canvas.parentElement.getBoundingClientRect();
        this.canvas.width = rect.width;
        this.canvas.height = rect.height;
    }

    /**
     * @method startSimulation
     * @description Begins price simulation with periodic updates
     * @author Vrushank Patel
     */
    startSimulation() {
        setInterval(() => {
            this.symbols.forEach(symbol => {
                const currentPrice = this.prices[symbol].price;
                const change = (Math.random() - 0.5) * 2;
                const newPrice = currentPrice + change;
                
                this.prices[symbol] = {
                    price: newPrice,
                    change: change
                };
                
                this.priceHistory[symbol].push({
                    price: newPrice,
                    timestamp: new Date()
                });
                
                const oneHourAgo = new Date(Date.now() - 60 * 60 * 1000);
                this.priceHistory[symbol] = this.priceHistory[symbol].filter(
                    p => p.timestamp > oneHourAgo
                );
            });
            
            this.render();
            this.renderChart();
        }, 1000);
    }

    /**
     * @method renderChart
     * @description Renders the price chart for the selected symbol
     * @author Vrushank Patel
     */
    renderChart() {
        const isDarkMode = document.documentElement.getAttribute('data-theme') === 'dark';
        const ctx = this.ctx;
        const data = this.priceHistory[this.selectedSymbol];
        
        ctx.fillStyle = isDarkMode ? '#1a1a1a' : '#000';
        ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
        
        if (data.length < 2) return;
        
        const prices = data.map(d => d.price);
        const minPrice = Math.min(...prices);
        const maxPrice = Math.max(...prices);
        const priceRange = maxPrice - minPrice;
        
        ctx.strokeStyle = isDarkMode ? '#444' : '#333';
        ctx.lineWidth = 1;
        
        for (let i = 0; i < 10; i++) {
            const x = (this.canvas.width / 9) * i;
            ctx.beginPath();
            ctx.moveTo(x, 0);
            ctx.lineTo(x, this.canvas.height);
            ctx.stroke();
        }
        
        for (let i = 0; i < 5; i++) {
            const y = (this.canvas.height / 4) * i;
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(this.canvas.width, y);
            ctx.stroke();
        }
        
        const gradient = ctx.createLinearGradient(0, 0, 0, this.canvas.height);
        gradient.addColorStop(0, isDarkMode ? '#00cccc' : '#00ffff');
        gradient.addColorStop(1, isDarkMode ? '#2ecc71' : '#32cd32');
        ctx.strokeStyle = gradient;
        ctx.lineWidth = 2;
        ctx.beginPath();
        
        data.forEach((point, i) => {
            const x = (i / (data.length - 1)) * this.canvas.width;
            const y = this.canvas.height - 
                     ((point.price - minPrice) / priceRange) * this.canvas.height;
            
            if (i === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        
        ctx.stroke();
        
        ctx.fillStyle = isDarkMode ? '#e6c200' : '#ffd700';
        ctx.font = '12px Courier New';
        ctx.textAlign = 'right';
        
        ctx.textAlign = 'center';
        ctx.fillStyle = isDarkMode ? '#777' : '#888';
        const now = new Date();
        for (let i = 0; i < 5; i++) {
            const x = this.canvas.width - (this.canvas.width / 4) * i;
            const time = new Date(now - (i * 15 * 60 * 1000));
            ctx.fillText(time.toLocaleTimeString(), x, this.canvas.height - 5);
        }
        
        ctx.textAlign = 'right';
        ctx.fillStyle = isDarkMode ? '#e6c200' : '#ffd700';
        for (let i = 0; i <= 4; i++) {
            const price = minPrice + (priceRange * (i / 4));
            const y = this.canvas.height - (this.canvas.height * (i / 4));
            ctx.fillText(price.toFixed(2), this.canvas.width - 5, y + 4);
        }
        
        const currentPrice = data[data.length - 1].price;
        ctx.fillStyle = isDarkMode ? '#e1e1e1' : '#fff';
        ctx.fillText(`Current: ${currentPrice.toFixed(2)}`, this.canvas.width - 5, 20);
    }

    /**
     * @method render
     * @description Renders the market data ticker
     * @author Vrushank Patel
     */
    render() {
        const container = document.getElementById('tickerContainer');
        container.innerHTML = this.symbols.map(symbol => `
            <div class="ticker-item">
                <span class="symbol">${symbol}</span>
                <span class="price">${this.prices[symbol].price.toFixed(2)}</span>
                <span class="change ${this.prices[symbol].change >= 0 ? 'positive' : 'negative'}">
                    ${this.prices[symbol].change.toFixed(2)}
                </span>
            </div>
        `).join('');
    }
}

const marketData = new MarketData(); 