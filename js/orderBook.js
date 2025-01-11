class OrderBook {
    constructor() {
        this.bids = [];
        this.asks = [];
        this.initializeEventListeners();
    }

    initializeEventListeners() {
        document.addEventListener('newOrder', (e) => this.handleNewOrder(e.detail));
    }

    handleNewOrder(order) {
        if (order.isCombo) {
            this.handleComboOrder(order);
        } else {
            if (order.side === 'BUY') {
                this.bids.push(order);
                this.bids.sort((a, b) => b.price - a.price);
            } else {
                this.asks.push(order);
                this.asks.sort((a, b) => a.price - b.price);
            }
        }

        this.render();
        this.checkForMatches();
    }

    handleComboOrder(order) {
        // Create individual orders for each leg
        order.legs.forEach(leg => {
            const legOrder = new Order(
                leg.symbol,
                order.orderType,
                leg.side,
                order.quantity * leg.ratio,
                order.price,
                order.stopPrice,
                {
                    senderCompId: order.senderCompId,
                    senderSubId: order.senderSubId,
                    targetCompId: order.targetCompId,
                    clOrdId: `${order.clOrdId}_${leg.symbol}`
                }
            );
            
            this.handleNewOrder(legOrder);
        });
    }

    checkForMatches() {
        while (this.bids.length > 0 && this.asks.length > 0) {
            const topBid = this.bids[0];
            const topAsk = this.asks[0];

            if (topBid.price >= topAsk.price) {
                const matchedQuantity = Math.min(topBid.quantity, topAsk.quantity);
                const matchPrice = topAsk.price;

                // Create trade
                const trade = {
                    price: matchPrice,
                    quantity: matchedQuantity,
                    timestamp: new Date(),
                    buyOrderId: topBid.orderId,
                    sellOrderId: topAsk.orderId
                };

                // Dispatch trade event
                document.dispatchEvent(new CustomEvent('newTrade', { detail: trade }));

                // Update quantities
                topBid.quantity -= matchedQuantity;
                topAsk.quantity -= matchedQuantity;

                // Remove filled orders
                if (topBid.quantity === 0) this.bids.shift();
                if (topAsk.quantity === 0) this.asks.shift();
            } else {
                break;
            }
        }

        this.render();
    }

    render() {
        const bidsContainer = document.getElementById('bids');
        const asksContainer = document.getElementById('asks');

        bidsContainer.innerHTML = '<h3>Bids</h3>' + this.bids.map(order => `
            <div class="order-item">
                <span>${order.price}</span>
                <span>${order.quantity}</span>
            </div>
        `).join('');

        asksContainer.innerHTML = '<h3>Asks</h3>' + this.asks.map(order => `
            <div class="order-item">
                <span>${order.price}</span>
                <span>${order.quantity}</span>
            </div>
        `).join('');
    }
}

// Initialize order book
const orderBook = new OrderBook(); 