import React, { useState, useEffect, useRef } from 'react';

const UserEditForm = ({ initialData = {}, editing = false, onCancel, onSave }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phone: '',
    address: '',
    bio: '',
    avatar: null,
    ...initialData
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const fileRef = useRef(null);

  useEffect(() => {
    setFormData(prev => ({ ...prev, ...initialData }));
  }, [initialData]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const handleImage = (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (ev) => setFormData(prev => ({ ...prev, avatar: ev.target.result }));
    reader.readAsDataURL(file);
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name || !formData.name.trim()) newErrors.name = 'El nombre es requerido';
    if (!formData.email || !/\S+@\S+\.\S+/.test(formData.email)) newErrors.email = 'Email inválido';
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e && e.preventDefault();
    const validation = validate();
    if (Object.keys(validation).length > 0) {
      setErrors(validation);
      return;
    }

    if (typeof onSave === 'function') {
      try {
        setLoading(true);
        await onSave({
          name: formData.name,
          email: formData.email,
          phone: formData.phone,
          address: formData.address,
          bio: formData.bio,
          avatar: formData.avatar
        });
      } finally {
        setLoading(false);
      }
    }
  };

  if (!editing) return null;

  return (
    <form onSubmit={handleSubmit} style={{ marginBottom: '1rem' }}>
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Nombre Completo</label>
          <input name="name" value={formData.name || ''} onChange={handleChange} />
          {errors.name && <div style={{ color: '#EF4444' }}>{errors.name}</div>}
        </div>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Email</label>
          <input name="email" value={formData.email || ''} onChange={handleChange} />
          {errors.email && <div style={{ color: '#EF4444' }}>{errors.email}</div>}
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem', marginBottom: '1rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Teléfono</label>
          <input name="phone" value={formData.phone || ''} onChange={handleChange} />
        </div>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Dirección</label>
          <input name="address" value={formData.address || ''} onChange={handleChange} />
        </div>
      </div>

      <div style={{ marginBottom: '1rem' }}>
        <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Biografía</label>
        <textarea name="bio" value={formData.bio || ''} onChange={handleChange} rows={3} />
      </div>

      <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', marginBottom: '1rem' }}>
        <div>
          <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Avatar</label>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <div style={{ width: 64, height: 64, borderRadius: 8, background: formData.avatar ? `url(${formData.avatar}) center/cover` : '#E5E7EB' }} />
            <div>
              <input ref={fileRef} type="file" accept="image/*" onChange={handleImage} />
            </div>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
        {onCancel && (
          <button type="button" onClick={onCancel} style={{ padding: '0.5rem 1rem' }}>Cancelar</button>
        )}
        <button type="submit" disabled={loading} style={{ padding: '0.5rem 1rem', background: '#10B981', color: 'white', border: 'none' }}>
          {loading ? 'Guardando...' : 'Guardar Cambios'}
        </button>
      </div>
    </form>
  );
};

export default UserEditForm;
