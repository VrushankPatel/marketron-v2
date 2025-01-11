class OrderBook {
    constructor() {
        this.bids = [];
        this.asks = [];
        this.loadPersistedData();
        this.initializeEventListeners();
        this.render();
    }

    loadPersistedData() {
        const data = persistenceService.loadData();
        if (data && data.orderBook) {
            this.bids = data.orderBook.bids || [];
            this.asks = data.orderBook.asks || [];
            
            this.bids.forEach(bid => bid.timestamp = new Date(bid.timestamp));
            this.asks.forEach(ask => ask.timestamp = new Date(ask.timestamp));
        }
    }

    persistData() {
        const currentData = persistenceService.loadData() || {};
        currentData.orderBook = {
            bids: this.bids,
            asks: this.asks
        };
        persistenceService.saveData(currentData);
    }

    initializeEventListeners() {
        console.log('Initializing OrderBook event listeners');
        document.addEventListener('newOrder', (e) => this.handleNewOrder(e.detail));
    }

    handleNewOrder(order) {
        console.log('OrderBook received new order:', order);
        if (order.orderType === 'MARKET') {
            if (order.side === 'BUY' && this.asks.length > 0) {
                order.price = this.asks[0].price;
            } else if (order.side === 'SELL' && this.bids.length > 0) {
                order.price = this.bids[0].price;
            } else {
                const currentPrice = marketData.prices[order.symbol]?.price;
                if (!currentPrice) {
                    Swal.fire({
                        title: 'Market Order Error',
                        text: 'Unable to determine market price. Please try again.',
                        icon: 'error',
                        confirmButtonText: 'OK'
                    });
                    return;
                }
                order.price = currentPrice;
            }
        }

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
        this.persistData();
    }

    handleComboOrder(order) {
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

                const trade = {
                    price: matchPrice,
                    quantity: matchedQuantity,
                    timestamp: new Date(),
                    buyOrderId: topBid.orderId,
                    sellOrderId: topAsk.orderId
                };

                document.dispatchEvent(new CustomEvent('newTrade', { detail: trade }));

                topBid.quantity -= matchedQuantity;
                topAsk.quantity -= matchedQuantity;

                if (topBid.quantity === 0) this.bids.shift();
                if (topAsk.quantity === 0) this.asks.shift();

                snackbarService.show(`Trade executed: ${matchedQuantity} @ ${matchPrice.toFixed(2)}`);
            } else {
                break;
            }
        }

        this.render();
    }

    render() {
        console.log('Rendering OrderBook - Bids:', this.bids, 'Asks:', this.asks);
        const bidsContainer = document.getElementById('bids');
        const asksContainer = document.getElementById('asks');

        bidsContainer.innerHTML = '<h3>Bids</h3>' + this.bids.map(order => `
            <div class="order-item">
                <span class="order-symbol">${order.symbol}</span>
                <span class="order-price">${order.price ? order.price.toFixed(2) : 'MKT'}</span>
                <span class="order-quantity">${order.quantity}</span>
            </div>
        `).join('');

        asksContainer.innerHTML = '<h3>Asks</h3>' + this.asks.map(order => `
            <div class="order-item">
                <span class="order-symbol">${order.symbol}</span>
                <span class="order-price">${order.price ? order.price.toFixed(2) : 'MKT'}</span>
                <span class="order-quantity">${order.quantity}</span>
            </div>
        `).join('');
    }
}

const orderBook = new OrderBook(); 