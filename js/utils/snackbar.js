/**
 * @class SnackbarService
 * @description Handles snackbar notifications
 * @author Vrushank Patel
 */
class SnackbarService {
    constructor() {
        this.init();
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
     * @author Vrushank Patel
     */
    show(message, type = 'success') {
        const snackbar = document.getElementById('snackbar');
        snackbar.textContent = message;
        snackbar.className = 'snackbar show ' + type;

        setTimeout(() => {
            snackbar.className = snackbar.className.replace('show', '');
        }, 3000);
    }
}

const snackbarService = new SnackbarService(); 