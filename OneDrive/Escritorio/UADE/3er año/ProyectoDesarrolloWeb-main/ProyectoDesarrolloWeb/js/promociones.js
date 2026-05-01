// promociones.js - cálculos de promociones para la página promociones.html
(function(){
  'use strict'

  const prices = {
    sahumerios: 8500,
    difusores: 12000,
    aromatizantes: 6500
  }

  function formatCurrency(value){
    try{
      return new Intl.NumberFormat('es-AR',{style:'currency', currency:'ARS', maximumFractionDigits:0}).format(value)
    }catch(e){
      return '$' + Math.round(value).toLocaleString()
    }
  }

  function readInputs(){
    const qEls = Array.from(document.querySelectorAll('.qty'))
    const qty = {}
    qEls.forEach(el => { qty[el.dataset.id] = Math.max(0, parseInt(el.value) || 0) })
    const promo = (document.querySelector('input[name=promo]:checked')||{}).value || 'promo1'
    return {qty, promo}
  }

  function calculate({qty, threex2, promo}){
    // total sin descuento
    let subtotal = 0
    Object.keys(prices).forEach(k => { subtotal += (prices[k] * (qty[k] || 0)) })

    let discount = 0
    let breakdown = []

    if(promo === 'promo1'){
      // Llevá 2 y obtené 50% en el segundo (por cada par de la misma referencia)
      Object.keys(prices).forEach(k => {
        const q = qty[k] || 0
        const pairs = Math.floor(q / 2)
        const d = pairs * prices[k] * 0.5
        if(d > 0) breakdown.push({label: `50% en el segundo - ${k}`, value: d})
        discount += d
      })
    } else if(promo === 'promo2'){
      // 3x2 en todos los productos (se aplica a todas las referencias con qty>0)
      Object.keys(prices).forEach(k => {
        const q = qty[k] || 0
        const triples = Math.floor(q / 3)
        const d = triples * prices[k]
        if(d > 0) breakdown.push({label: `3x2 - ${k}`, value: d})
        discount += d
      })
    } else if(promo === 'promo3'){
      // 10% por compras superiores a $30.000 (sobre el subtotal)
      if(subtotal > 30000){
        discount = subtotal * 0.10
        breakdown.push({label: '10% sobre total > $30.000', value: discount})
      }
    }

    const total = Math.max(0, subtotal - discount)
    return {subtotal, discount, total, breakdown}
  }

  function renderResults(res){
    const el = document.getElementById('results')
    if(!el) return
    el.innerHTML = ''

    const subP = document.createElement('p')
    subP.innerHTML = `<strong>Total sin descuento:</strong> ${formatCurrency(res.subtotal)}`
    el.appendChild(subP)

    if(res.breakdown.length){
      const list = document.createElement('ul')
      list.className = 'list-unstyled small'
      res.breakdown.forEach(b => {
        const li = document.createElement('li')
        li.textContent = `${b.label}: - ${formatCurrency(b.value)}`
        list.appendChild(li)
      })
      el.appendChild(list)
    }

    const discP = document.createElement('p')
    discP.innerHTML = `<strong>Descuento aplicado:</strong> ${formatCurrency(res.discount)}`
    el.appendChild(discP)

    const totalP = document.createElement('p')
    totalP.innerHTML = `<strong>Total final:</strong> ${formatCurrency(res.total)}`
    el.appendChild(totalP)

    // Friendly message
    const msg = document.createElement('div')
    msg.className = 'alert alert-light small'
    if(res.discount <= 0){
      msg.textContent = 'No se aplicó ningún descuento con los valores ingresados. Probá ajustar cantidades o elegir otra promoción.'
    } else {
      msg.textContent = `¡Buen ahorro! Estás ahorrando ${formatCurrency(res.discount)}.`
    }
    el.appendChild(msg)

    // enable checkout if total > 0
    const checkout = document.getElementById('checkout')
    if(checkout){
      checkout.disabled = res.total <= 0
      checkout.title = res.total > 0 ? 'Finalizar compra (simulado)' : ''
    }
  }

  function attach(){
    const calcBtn = document.getElementById('calcBtn')
    if(calcBtn) calcBtn.addEventListener('click', ()=>{
      const inputs = readInputs()
      // validation: prevent accidental entry of price instead of quantity
      const maxReasonable = 1000 // umbral para detectar entradas erróneas (puede ajustarse)
      const huge = Object.values(inputs.qty).some(v => v > maxReasonable)
      if(huge){
        const el = document.getElementById('results')
        if(el){
          el.innerHTML = ''
          const p = document.createElement('div')
          p.className = 'alert alert-warning small'
          p.innerHTML = '<strong>Revisá las cantidades:</strong> parece que ingresaste un valor muy alto (por ejemplo un precio en lugar de la cantidad). Introducí números pequeños (1, 2, 3) en las cantidades.'
          el.appendChild(p)
        }
        return
      }

      const res = calculate(inputs)
      renderResults(res)
    })

    const form = document.getElementById('promoForm')
    if(form) form.addEventListener('reset', ()=>{
      setTimeout(()=>{
        const results = document.getElementById('results')
        if(results) results.innerHTML = '<p class="small text-muted">Los resultados se mostrarán aquí luego de hacer clic en <strong>Calcular</strong>.</p>'
        const checkout = document.getElementById('checkout')
        if(checkout) checkout.disabled = true
      }, 30)
    })

    const checkout = document.getElementById('checkout')
    if(checkout) checkout.addEventListener('click', ()=>{
      alert('Proceso de compra simulado. Gracias por su pedido.')
    })

    // UX: si el usuario marca la casilla 3x2 junto al producto, seleccionamos
    // automáticamente la promoción 'promo2' para evitar confusiones.
    const thEls = Array.from(document.querySelectorAll('.threex2'))
    thEls.forEach(el => {
      el.addEventListener('change', (e) => {
        if(e.target.checked){
          const promo2 = document.getElementById('promo2')
          if(promo2){ promo2.checked = true; promo2.dispatchEvent(new Event('change')) }
        }
      })
    })

    // Habilitar / deshabilitar las casillas 3x2 según la promo seleccionada
    const promoRadios = Array.from(document.querySelectorAll('input[name=promo]'))
    function updateThreex2State(){
      const current = (document.querySelector('input[name=promo]:checked')||{}).value || 'promo1'
      const enabled = current === 'promo2'
      thEls.forEach(el => { el.disabled = !enabled })
    }
    promoRadios.forEach(r => r.addEventListener('change', updateThreex2State))
    // initial state
    updateThreex2State()
  }

  // Initialize when DOM ready
  if(document.readyState === 'loading'){
    document.addEventListener('DOMContentLoaded', attach)
  } else attach()

})();
