// static/js/invoice.js
document.addEventListener('DOMContentLoaded', function() {
	const itemsContainer = document.getElementById('items-container');
	const addItemBtn = document.getElementById('add-item');
	const productPrices = {};

	// Initialize product prices from options
	document.querySelectorAll('.product-select option').forEach(option => {
		if (option.value) {
			const match = option.text.match(/₹(\d+\.?\d*)/);
			if (match) {
				productPrices[option.value] = parseFloat(match[1]);
			}
		}
	});

	addItemBtn.addEventListener('click', function() {
		const index = document.querySelectorAll('.item-row').length;
		const newRow = document.createElement('div');
		newRow.className = 'item-row row mb-2';
		newRow.innerHTML = `
            <div class="col-md-4">
                <select class="form-select product-select" name="items[${index}].productId" required>
                    <option value="">Select Product</option>
                    ${document.querySelector('.product-select').innerHTML}
                </select>
            </div>
            <div class="col-md-2">
                <input type="number" class="form-control quantity" name="items[${index}].quantity" 
                       min="1" value="1" required>
            </div>
            <div class="col-md-4">
                <span class="item-total">₹0.00</span>
            </div>
            <div class="col-md-2">
                <button type="button" class="btn btn-danger btn-sm remove-item">Remove</button>
            </div>
        `;
		itemsContainer.appendChild(newRow);
		attachEventListeners(newRow);
	});

	function attachEventListeners(row) {
		const productSelect = row.querySelector('.product-select');
		const quantityInput = row.querySelector('.quantity');
		const removeBtn = row.querySelector('.remove-item');

		productSelect.addEventListener('change', calculateTotals);
		quantityInput.addEventListener('input', calculateTotals);
		removeBtn.addEventListener('click', function() {
			row.remove();
			calculateTotals();
		});
	}

	function calculateTotals() {
		let subtotal = 0;
		let totalGst = 0;

		document.querySelectorAll('.item-row').forEach(row => {
			const productId = row.querySelector('.product-select').value;
			const quantity = parseInt(row.querySelector('.quantity').value) || 0;
			const itemTotal = row.querySelector('.item-total');

			if (productId && productPrices[productId]) {
				const price = productPrices[productId];
				const itemSubtotal = price * quantity;
				// For demo, using 18% GST - in real app, get from product data
				const gstRate = 0.18;
				const gstAmount = itemSubtotal * gstRate;
				const itemTotalAmount = itemSubtotal + gstAmount;

				itemTotal.textContent = `₹${itemTotalAmount.toFixed(2)}`;
				subtotal += itemSubtotal;
				totalGst += gstAmount;
			} else {
				itemTotal.textContent = '₹0.00';
			}
		});

		document.getElementById('subtotal').textContent = `₹${subtotal.toFixed(2)}`;
		document.getElementById('total-gst').textContent = `₹${totalGst.toFixed(2)}`;
		document.getElementById('total-amount').textContent = `₹${(subtotal + totalGst).toFixed(2)}`;
	}

	// Attach listeners to initial row
	document.querySelectorAll('.item-row').forEach(attachEventListeners);
	calculateTotals();
});