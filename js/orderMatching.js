/**
 * @class OrderMatching
 * @description Handles trade matching and execution
 * @author Vrushank Patel
 */
class OrderMatching {
    /**
     * @constructor
     * @author Vrushank Patel
     */
    constructor() {
        this.trades = [];
        this.initializeEventListeners();
    }

    /**
     * @method initializeEventListeners
     * @description Sets up event listeners for trade events
     * @author Vrushank Patel
     */
    initializeEventListeners() {
        document.addEventListener('newTrade', (e) => this.handleNewTrade(e.detail));
    }

    /**
     * @method handleNewTrade
     * @description Processes new trades and updates the display
     * @param {Object} trade - The trade object containing trade details
     * @author Vrushank Patel
     */
    handleNewTrade(trade) {
        this.trades.unshift(trade);
        this.render();
    }

    /**
     * @method render
     * @description Renders the trades list
     * @author Vrushank Patel
     */
    render() {
        const container = document.getElementById('tradesContainer');
        container.innerHTML = this.trades.map(trade => `
            <div class="trade-item">
                <span class="trade-price">Price: ${trade.price}</span>
                <span class="trade-quantity">Quantity: ${trade.quantity}</span>
                <span class="trade-time">Time: ${trade.timestamp.toLocaleTimeString()}</span>
            </div>
        `).join('');
    }
}

// Initialize order matching
const orderMatching = new OrderMatching(); 