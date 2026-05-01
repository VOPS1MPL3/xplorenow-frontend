// Validación simple de formulario usando clases de Bootstrap
(function(){
  'use strict'
  const form = document.getElementById('contactForm')
  if(!form) return
  form.addEventListener('submit', function(event){
    if(!form.checkValidity()){
      event.preventDefault();
      event.stopPropagation();
    } else {
      event.preventDefault();
      // Aquí podríamos hacer un fetch a un endpoint o simular envío
      alert('Mensaje enviado (simulado).');
      form.reset();
    }
    form.classList.add('was-validated')
  })
})();

// Back-to-top button: show on scroll and smooth-scroll to top on click
(function(){
  // Create button markup so it exists on any page including this script
  const existing = document.getElementById('btnTop')
  if(!existing){
    const btn = document.createElement('button')
    btn.className = 'btn-top'
    btn.id = 'btnTop'
    btn.setAttribute('aria-label','Subir al inicio')
    btn.setAttribute('title','Subir al inicio')
    btn.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true"><path d="M12 4L12 20" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/><path d="M5 11L12 4L19 11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>'
    document.body.appendChild(btn)
  }
  const btnEl = document.getElementById('btnTop')
  if(!btnEl) return
  const showAt = 300
  window.addEventListener('scroll', ()=>{
    if(window.scrollY > showAt){ btnEl.classList.add('show') } else { btnEl.classList.remove('show') }
  })
  btnEl.addEventListener('click', ()=>{ window.scrollTo({top:0, behavior:'smooth'}) })
})();

// Ajuste de posición por slide para banners (usa data-pos en .carousel-item)
(function(){
  const carousel = document.getElementById('heroCarousel')
  if(!carousel) return
  const items = carousel.querySelectorAll('.carousel-item')
  items.forEach(item => {
    const pos = item.getAttribute('data-pos') || 'center center'
    const img = item.querySelector('.carousel-img')
    if(img) img.style.objectPosition = pos
  })
  // Hacer pause on hover
  carousel.addEventListener('mouseenter', ()=>{ const bs = bootstrap.Carousel.getInstance(carousel); if(bs) bs.pause() })
  carousel.addEventListener('mouseleave', ()=>{ const bs = bootstrap.Carousel.getInstance(carousel); if(bs) bs.cycle() })
})();
