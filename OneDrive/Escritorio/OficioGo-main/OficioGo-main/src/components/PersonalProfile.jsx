import React, { useState, useRef } from 'react';
import { useAuth } from '../contexts/MockAuthContext';
import AuthNavbar from './AuthNavbar';
import { FaUser, FaCamera, FaLock, FaEye, FaEyeSlash, FaSave, FaEdit } from 'react-icons/fa';
import UserEditForm from './UserEditForm';

const PersonalProfile = () => {
  const { user, updateUserProfile } = useAuth();
  const [isEditing, setIsEditing] = useState(false);
  const [showCurrentPassword, setShowCurrentPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [profileImage, setProfileImage] = useState(user?.avatar || null);
  const fileInputRef = useRef(null);

  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: user?.address || '',
    bio: user?.bio || ''
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState('');

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    // Limpiar error cuando el usuario empiece a escribir
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handlePasswordChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const handleImageUpload = (e) => {
    const file = e.target.files[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (e) => {
        setProfileImage(e.target.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    if (!formData.name.trim()) {
      newErrors.name = 'El nombre es requerido';
    }
    
    if (!formData.email.trim()) {
      newErrors.email = 'El email es requerido';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email inválido';
    }

    return newErrors;
  };

  const validatePassword = () => {
    const newErrors = {};
    
    if (passwordData.newPassword && passwordData.newPassword.length < 6) {
      newErrors.newPassword = 'La nueva contraseña debe tener al menos 6 caracteres';
    }
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      newErrors.confirmPassword = 'Las contraseñas no coinciden';
    }

    return newErrors;
  };

  const handleSubmit = async (e) => {
    // kept for compatibility but form is now handled by UserEditForm
    e.preventDefault();
  };

  const handleSaveFromForm = async (data) => {
    try {
      await updateUserProfile({ ...data });
      setSuccess('Perfil actualizado correctamente');
      setIsEditing(false);
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setErrors({ general: 'Error al actualizar el perfil' });
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    const passwordErrors = validatePassword();
    
    if (Object.keys(passwordErrors).length > 0) {
      setErrors(passwordErrors);
      return;
    }

    try {
      // Simular cambio de contraseña
      setSuccess('Contraseña actualizada correctamente');
      setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
      setTimeout(() => setSuccess(''), 3000);
    } catch (error) {
      setErrors({ password: 'Error al cambiar la contraseña' });
    }
  };

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh' }}>
      <AuthNavbar />
      
      <div style={{ maxWidth: '800px', margin: '0 auto', padding: '2rem' }}>
        <div style={{ 
          background: 'white', 
          borderRadius: '12px', 
          boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden'
        }}>
          {/* Header */}
          <div style={{ 
            background: 'linear-gradient(135deg, #4A90E2, #7BB3F0)', 
            color: 'white', 
            padding: '2rem',
            textAlign: 'center'
          }}>
            <h1 style={{ margin: 0, fontSize: '2rem', fontWeight: 'bold' }}>
              <FaUser style={{ marginRight: '0.5rem' }} />
              Mi Perfil Personal
            </h1>
            <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9 }}>
              Administra tu información personal y configuración de cuenta
            </p>
          </div>

          {/* Success Message */}
          {success && (
            <div style={{
              background: '#D1FAE5',
              color: '#047857',
              padding: '1rem',
              textAlign: 'center',
              borderBottom: '1px solid #E5E7EB'
            }}>
              {success}
            </div>
          )}

          <div style={{ padding: '2rem' }}>
            {/* Profile Image Section */}
            <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
              <div style={{ position: 'relative', display: 'inline-block' }}>
                <div style={{
                  width: '120px',
                  height: '120px',
                  borderRadius: '50%',
                  background: profileImage ? `url(${profileImage}) center/cover` : '#E5E7EB',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '3rem',
                  color: '#9CA3AF',
                  border: '4px solid #4A90E2'
                }}>
                  {!profileImage && <FaUser />}
                </div>
                <button
                  onClick={() => fileInputRef.current?.click()}
                  style={{
                    position: 'absolute',
                    bottom: '0',
                    right: '0',
                    background: '#4A90E2',
                    color: 'white',
                    border: 'none',
                    borderRadius: '50%',
                    width: '36px',
                    height: '36px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                  }}
                >
                  <FaCamera />
                </button>
                <input
                  ref={fileInputRef}
                  type="file"
                  accept="image/*"
                  onChange={handleImageUpload}
                  style={{ display: 'none' }}
                />
              </div>
              <h3 style={{ marginTop: '1rem', color: '#374151' }}>{user?.name}</h3>
              <p style={{ color: '#6B7280', margin: 0 }}>{user?.role}</p>
            </div>

            {/* Personal Information Form */}
            <div style={{ marginBottom: '2rem' }}>
              <div style={{ 
                display: 'flex', 
                justifyContent: 'space-between', 
                alignItems: 'center', 
                marginBottom: '1.5rem' 
              }}>
                <h2 style={{ margin: 0, color: '#374151' }}>Información Personal</h2>
                <button
                  onClick={() => setIsEditing(!isEditing)}
                  style={{
                    background: isEditing ? '#EF4444' : '#4A90E2',
                    color: 'white',
                    border: 'none',
                    padding: '0.5rem 1rem',
                    borderRadius: '6px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem'
                  }}
                >
                  {isEditing ? 'Cancelar' : <><FaEdit /> Editar</>}
                </button>
              </div>

              <UserEditForm
                initialData={{
                  name: formData.name,
                  email: formData.email,
                  phone: formData.phone,
                  address: formData.address,
                  bio: formData.bio,
                  avatar: profileImage
                }}
                editing={isEditing}
                onCancel={() => setIsEditing(false)}
                onSave={handleSaveFromForm}
              />
            </div>

            {/* Password Change Section */}
            <div style={{ 
              borderTop: '1px solid #E5E7EB', 
              paddingTop: '2rem' 
            }}>
              <h2 style={{ marginBottom: '1.5rem', color: '#374151' }}>
                <FaLock style={{ marginRight: '0.5rem' }} />
                Cambiar Contraseña
              </h2>

              <form onSubmit={handlePasswordSubmit}>
                <div style={{ display: 'grid', gap: '1rem', maxWidth: '400px' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', color: '#374151' }}>
                      Contraseña Actual
                    </label>
                    <div style={{ position: 'relative' }}>
                      <input
                        type={showCurrentPassword ? 'text' : 'password'}
                        name="currentPassword"
                        value={passwordData.currentPassword}
                        onChange={handlePasswordChange}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          paddingRight: '3rem',
                          border: '1px solid #D1D5DB',
                          borderRadius: '6px',
                          boxSizing: 'border-box'
                        }}
                      />
                      <button
                        type="button"
                        onClick={() => setShowCurrentPassword(!showCurrentPassword)}
                        style={{
                          position: 'absolute',
                          right: '0.75rem',
                          top: '50%',
                          transform: 'translateY(-50%)',
                          background: 'none',
                          border: 'none',
                          cursor: 'pointer',
                          color: '#6B7280'
                        }}
                      >
                        {showCurrentPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', color: '#374151' }}>
                      Nueva Contraseña
                    </label>
                    <div style={{ position: 'relative' }}>
                      <input
                        type={showNewPassword ? 'text' : 'password'}
                        name="newPassword"
                        value={passwordData.newPassword}
                        onChange={handlePasswordChange}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          paddingRight: '3rem',
                          border: `1px solid ${errors.newPassword ? '#EF4444' : '#D1D5DB'}`,
                          borderRadius: '6px',
                          boxSizing: 'border-box'
                        }}
                      />
                      <button
                        type="button"
                        onClick={() => setShowNewPassword(!showNewPassword)}
                        style={{
                          position: 'absolute',
                          right: '0.75rem',
                          top: '50%',
                          transform: 'translateY(-50%)',
                          background: 'none',
                          border: 'none',
                          cursor: 'pointer',
                          color: '#6B7280'
                        }}
                      >
                        {showNewPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    {errors.newPassword && <p style={{ color: '#EF4444', fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>{errors.newPassword}</p>}
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500', color: '#374151' }}>
                      Confirmar Nueva Contraseña
                    </label>
                    <div style={{ position: 'relative' }}>
                      <input
                        type={showConfirmPassword ? 'text' : 'password'}
                        name="confirmPassword"
                        value={passwordData.confirmPassword}
                        onChange={handlePasswordChange}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          paddingRight: '3rem',
                          border: `1px solid ${errors.confirmPassword ? '#EF4444' : '#D1D5DB'}`,
                          borderRadius: '6px',
                          boxSizing: 'border-box'
                        }}
                      />
                      <button
                        type="button"
                        onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        style={{
                          position: 'absolute',
                          right: '0.75rem',
                          top: '50%',
                          transform: 'translateY(-50%)',
                          background: 'none',
                          border: 'none',
                          cursor: 'pointer',
                          color: '#6B7280'
                        }}
                      >
                        {showConfirmPassword ? <FaEyeSlash /> : <FaEye />}
                      </button>
                    </div>
                    {errors.confirmPassword && <p style={{ color: '#EF4444', fontSize: '0.875rem', margin: '0.25rem 0 0 0' }}>{errors.confirmPassword}</p>}
                  </div>

                  <button
                    type="submit"
                    style={{
                      background: '#4A90E2',
                      color: 'white',
                      border: 'none',
                      padding: '0.75rem 1.5rem',
                      borderRadius: '6px',
                      cursor: 'pointer',
                      fontWeight: '500'
                    }}
                  >
                    Cambiar Contraseña
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PersonalProfile;