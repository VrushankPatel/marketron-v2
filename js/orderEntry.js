class Order {
    constructor(symbol, orderType, side, quantity, price, stopPrice = null, orderConfig = {}) {
        this.orderId = Math.random().toString(36).substr(2, 9);
        this.symbol = symbol;
        this.orderType = orderType;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.stopPrice = stopPrice;
        this.timestamp = new Date();
        this.status = 'NEW';
        
        // FIX-related fields
        this.senderCompId = orderConfig.senderCompId || '';
        this.senderSubId = orderConfig.senderSubId || '';
        this.targetCompId = orderConfig.targetCompId || '';
        this.clOrdId = orderConfig.clOrdId || this.generateClOrdId();
        
        // Combo order fields
        this.isCombo = orderConfig.isCombo || false;
        this.legs = orderConfig.legs || [];
    }

    generateClOrdId() {
        return `ORD${Date.now()}${Math.floor(Math.random() * 1000)}`;
    }
}

class OrderLeg {
    constructor(symbol, ratio, side) {
        this.symbol = symbol;
        this.ratio = ratio;
        this.side = side;
    }
}

class OrderEntry {
    constructor() {
        this.form = document.getElementById('orderForm');
        this.initializeEventListeners();
        this.comboLegs = [];
    }

    initializeEventListeners() {
        this.form.addEventListener('submit', this.handleSubmit.bind(this));
        
        // Show/hide price fields based on order type
        document.getElementById('orderType').addEventListener('change', (e) => {
            const priceField = document.querySelector('.price-field');
            const stopPriceField = document.querySelector('.stop-price-field');
            
            switch(e.target.value) {
                case 'MARKET':
                    priceField.style.display = 'none';
                    stopPriceField.style.display = 'none';
                    break;
                case 'LIMIT':
                    priceField.style.display = 'block';
                    stopPriceField.style.display = 'none';
                    break;
                case 'STOP':
                    priceField.style.display = 'none';
                    stopPriceField.style.display = 'block';
                    break;
                case 'STOP_LIMIT':
                    priceField.style.display = 'block';
                    stopPriceField.style.display = 'block';
                    break;
            }
        });

        document.getElementById('orderCategory').addEventListener('change', (e) => {
            const comboFields = document.querySelector('.combo-fields');
            comboFields.style.display = e.target.value === 'COMBO' ? 'block' : 'none';
        });

        document.getElementById('addLeg').addEventListener('click', (e) => {
            e.preventDefault();
            this.addComboLeg();
        });
    }

    addComboLeg() {
        const leg = {
            symbol: document.getElementById('legSymbol').value,
            ratio: parseInt(document.getElementById('legRatio').value),
            side: document.getElementById('legSide').value
        };
        
        this.comboLegs.push(leg);
        this.renderComboLegs();
        
        // Clear leg input fields
        document.getElementById('legSymbol').value = '';
        document.getElementById('legRatio').value = '1';
    }

    renderComboLegs() {
        const container = document.getElementById('comboLegsContainer');
        container.innerHTML = this.comboLegs.map((leg, index) => `
            <div class="combo-leg">
                ${leg.symbol} (${leg.ratio}:1) ${leg.side}
                <button onclick="orderEntry.removeLeg(${index})">Remove</button>
            </div>
        `).join('');
    }

    removeLeg(index) {
        this.comboLegs.splice(index, 1);
        this.renderComboLegs();
    }

    handleSubmit(e) {
        e.preventDefault();
        
        const orderConfig = {
            senderCompId: document.getElementById('senderCompId').value,
            senderSubId: document.getElementById('senderSubId').value,
            targetCompId: document.getElementById('targetCompId').value,
            isCombo: document.getElementById('orderCategory').value === 'COMBO',
            legs: this.comboLegs
        };

        const order = new Order(
            document.getElementById('symbol').value,
            document.getElementById('orderType').value,
            document.getElementById('side').value,
            parseFloat(document.getElementById('quantity').value),
            parseFloat(document.getElementById('price').value) || null,
            parseFloat(document.getElementById('stopPrice').value) || null,
            orderConfig
        );

        document.dispatchEvent(new CustomEvent('newOrder', { detail: order }));
        
        // Reset form
        this.form.reset();
        this.comboLegs = [];
        this.renderComboLegs();
    }
}

// Initialize order entry
const orderEntry = new OrderEntry(); 