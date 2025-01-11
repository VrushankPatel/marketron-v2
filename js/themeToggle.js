class ThemeToggle {
    /**
     * @constructor
     * @author Vrushank Patel
     */
    constructor() {
        this.theme = localStorage.getItem('theme') || 'light';
        this.toggleButton = document.getElementById('themeToggle');
        this.initialize();
    }

    /**
     * @method initialize
     * @description Sets up initial theme and event listeners
     * @author Vrushank Patel
     */
    initialize() {
        if (this.theme === 'dark') {
            document.documentElement.setAttribute('data-theme', 'dark');
        }

        this.toggleButton.addEventListener('click', () => this.toggleTheme());
    }

    /**
     * @method toggleTheme
     * @description Toggles between light and dark themes
     * @author Vrushank Patel
     */
    toggleTheme() {
        this.theme = this.theme === 'light' ? 'dark' : 'light';
        document.documentElement.setAttribute('data-theme', this.theme);
        localStorage.setItem('theme', this.theme);
    }
}

const themeToggle = new ThemeToggle(); 