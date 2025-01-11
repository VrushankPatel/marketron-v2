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
        this.loadPersistedData();
        this.initializeEventListeners();
        this.render();
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
        this.persistData();
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

    loadPersistedData() {
        const data = persistenceService.loadData();
        if (data && data.trades) {
            this.trades = data.trades || [];
            this.trades.forEach(trade => trade.timestamp = new Date(trade.timestamp));
        }
    }

    persistData() {
        const currentData = persistenceService.loadData() || {};
        currentData.trades = this.trades;
        persistenceService.saveData(currentData);
    }
}

// Marketron entry point
const orderMatching = new OrderMatching(); 