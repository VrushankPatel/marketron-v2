/**
 * @class SnackbarService
 * @description Handles snackbar notifications with queue support
 * @author Vrushank Patel
 */
class SnackbarService {
    constructor() {
        this.init();
        this.queue = [];
        this.isShowing = false;
    }

    /**
     * @method init
     * @description Initializes the snackbar element
     * @author Vrushank Patel
     */
    init() {
        if (!document.getElementById('snackbar')) {
            const snackbar = document.createElement('div');
            snackbar.id = 'snackbar';
            snackbar.className = 'snackbar';
            document.body.appendChild(snackbar);
        }
    }

    /**
     * @method show
     * @description Shows a snackbar message
     * @param {string} message - Message to display
     * @param {string} type - Type of message (success/error)
     */
    show(message, type = 'success') {
        this.queue.push({ message, type });
        if (!this.isShowing) {
            this.showNext();
        }
    }

    /**
     * @method showNext
     * @description Shows the next message in queue
     */
    showNext() {
        if (this.queue.length === 0) {
            this.isShowing = false;
            return;
        }

        this.isShowing = true;
        const { message, type } = this.queue.shift();
        const snackbar = document.getElementById('snackbar');
        snackbar.textContent = message;
        snackbar.className = 'snackbar show ' + type;

        setTimeout(() => {
            snackbar.className = snackbar.className.replace('show', '');
            setTimeout(() => {
                this.showNext();
            }, 300); // Wait for fade out animation
        }, 3000);
    }
}

const snackbarService = new SnackbarService(); 