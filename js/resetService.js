/**
 * @class ResetService
 * @description Handles resetting the application state
 * @author Vrushank Patel
 */
class ResetService {
    constructor() {
        this.initializeEventListeners();
    }

    /**
     * @method initializeEventListeners
     * @description Sets up event listeners for reset functionality
     * @author Vrushank Patel
     */
    initializeEventListeners() {
        document.getElementById('resetButton').addEventListener('click', () => {
            this.confirmReset();
        });
    }

    /**
     * @method confirmReset
     * @description Shows confirmation dialog before reset
     * @author Vrushank Patel
     */
    confirmReset() {
        Swal.fire({
            title: 'Reset Marketron?',
            text: 'This will clear all orders, trades, and data. This action cannot be undone.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Yes, reset it!',
            cancelButtonText: 'Cancel'
        }).then((result) => {
            if (result.isConfirmed) {
                this.resetApplication();
            }
        });
    }

    /**
     * @method resetApplication
     * @description Resets the application state
     * @author Vrushank Patel
     */
    resetApplication() {
        localStorage.removeItem(persistenceService.STORAGE_KEY);

        Swal.fire({
            title: 'Reset Complete',
            text: 'Marketron has been reset successfully.',
            icon: 'success',
            confirmButtonText: 'OK'
        }).then(() => {
            window.location.href = window.location.href;
        });
    }
}

const resetService = new ResetService(); 