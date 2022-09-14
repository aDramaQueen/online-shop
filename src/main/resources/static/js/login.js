'use strict';

/**
 * Execute if site is finished loading on client side
 */
document.addEventListener("DOMContentLoaded", function(event) {

    const login_section_button = document.getElementById("login-section");
    const register_section_button = document.getElementById("register-section");
    const sliding_window = document.getElementById("sliding_window");

    login_section_button.addEventListener("click", function(event) {
        slide(sliding_window, login_section_button, register_section_button);
    });

    register_section_button.addEventListener("click", function(event) {
        slide(sliding_window, login_section_button, register_section_button);
    });

});

/**
 * This function executes the "sliding" effect, by toggling classes of certain HTML elements.
 * The actual work is done by CSS.
 *
 * @param {HTMLElement} sliding_login_wrapper
 * @param {HTMLElement} login_section_button
 * @param {HTMLElement} register_section_button
 */
function slide(sliding_login_wrapper, login_section_button, register_section_button) {
    sliding_login_wrapper.classList.toggle('move');
    sliding_login_wrapper.classList.toggle('active');
    login_section_button.classList.toggle('active');
    register_section_button.classList.toggle('active');

    // Deactivate error message for other section, if you trigger the sliding effect
    const error_message = document.getElementById("error-message");
    if (error_message !== null) {
        if (error_message.style.display === "none") {
            error_message.style.display = "block";
        } else {
            error_message.style.display = "none";
        }
    }
}