import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/MockAuthContext';
import mockApiService from '../services/mockApiService';
import UserEditForm from './UserEditForm';

const ProfilePage = () => {
  const { user, isProfessional, isClient, logout } = useAuth();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('info');
  const [editing, setEditing] = useState(false);
  const [profileData, setProfileData] = useState({});
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) {
      navigate('/login');
      return;
    }

    const loadProfileData = async () => {
      try {
        setLoading(true);
        
        // Cargar datos del usuario completo
        const userData = await mockApiService.getUserById(user.id);
        setProfileData(userData);

        // Cargar estadísticas del usuario  
        const userStats = await mockApiService.getUserStats(user.id);
        setStats(userStats);
        
      } catch (error) {
        console.error('Error loading profile:', error);
        alert('Error al cargar el perfil. Por favor, intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    };

    loadProfileData();
  }, [user, navigate]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setProfileData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProfile = async (data) => {
    try {
      // Guardar perfil usando el servicio API
      const updated = await mockApiService.updateUser(user.id, data);
      setProfileData(updated);
      alert('Perfil actualizado exitosamente');
      setEditing(false);
    } catch (error) {
      console.error('Error updating profile:', error);
      alert('Error al actualizar el perfil. Por favor, intenta nuevamente.');
    }
  };

  const handleLogout = () => {
    if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
      logout();
      navigate('/');
    }
  };

  if (!user) {
    return null;
  }

  if (loading) {
    return (
      <div style={{
        background: '#f8fafc',
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>👤</div>
          <p style={{ color: '#6b7280' }}>Cargando perfil...</p>
        </div>
      </div>
    );
  }

  const tabs = [
    { id: 'info', label: 'Información Personal', icon: '👤' },
    { id: 'stats', label: isProfessional() ? 'Estadísticas' : 'Mi Actividad', icon: '📊' },
    { id: 'history', label: isProfessional() ? 'Trabajos' : 'Historial', icon: '📋' },
    { id: 'reviews', label: 'Reseñas', icon: '⭐' },
    { id: 'settings', label: 'Configuración', icon: '⚙️' }
  ];

  return (
    <div style={{ background: '#f8fafc', minHeight: '100vh' }}>
      {/* Header */}
      <div style={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        padding: '2rem'
      }}>
        <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '2rem' }}>
            <div style={{
              fontSize: '4rem',
              background: 'rgba(255,255,255,0.2)',
              borderRadius: '50%',
              width: '5rem',
              height: '5rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              {profileData.profileImage}
            </div>
            <div>
              <h1 style={{ fontSize: '2rem', marginBottom: '0.5rem' }}>
                {profileData.name}
              </h1>
              <p style={{ opacity: 0.9, marginBottom: '0.5rem' }}>
                {isProfessional() ? 'Profesional' : 'Cliente'} • {profileData.email}
              </p>
              {isProfessional() && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span>⭐ {stats.rating}/5 ({stats.totalReviews} reseñas)</span>
                  <span>🔧 {stats.totalJobs} trabajos completados</span>
                </div>
              )}
            </div>
            <div style={{ marginLeft: 'auto' }}>
              <button
                onClick={() => setEditing(!editing)}
                style={{
                  background: 'rgba(255,255,255,0.2)',
                  border: '1px solid rgba(255,255,255,0.3)',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer',
                  marginRight: '1rem'
                }}
              >
                {editing ? 'Cancelar' : 'Editar Perfil'}
              </button>
              <button
                onClick={handleLogout}
                style={{
                  background: '#dc2626',
                  border: 'none',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer'
                }}
              >
                Cerrar Sesión
              </button>
            </div>
          </div>
        </div>
      </div>

      <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '2rem' }}>
        {/* Tabs */}
        <div style={{
          display: 'flex',
          gap: '0.5rem',
          marginBottom: '2rem',
          background: 'white',
          padding: '0.5rem',
          borderRadius: '0.75rem',
          boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
        }}>
          {tabs.map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              style={{
                padding: '1rem 1.5rem',
                border: 'none',
                borderRadius: '0.5rem',
                background: activeTab === tab.id 
                  ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)'
                  : 'transparent',
                color: activeTab === tab.id ? 'white' : '#6b7280',
                cursor: 'pointer',
                fontWeight: activeTab === tab.id ? '600' : '400',
                display: 'flex',
                alignItems: 'center',
                gap: '0.5rem',
                flex: 1,
                justifyContent: 'center'
              }}
            >
              <span>{tab.icon}</span>
              {tab.label}
            </button>
          ))}
        </div>

        {/* Tab Content */}
        {activeTab === 'info' && (
          <div style={{
            background: 'white',
            borderRadius: '0.75rem',
            padding: '2rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h2 style={{ marginBottom: '2rem', color: '#1f2937' }}>Información Personal</h2>
            
              <div style={{ display: 'grid', gap: '1.5rem' }}>
              {/* Información básica */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
                gap: '1rem'
              }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                    Nombre Completo
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={profileData.name}
                    onChange={handleInputChange}
                    disabled={!editing}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.5rem',
                      background: editing ? 'white' : '#f9fafb'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                    Email
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={profileData.email}
                    onChange={handleInputChange}
                    disabled={!editing}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.5rem',
                      background: editing ? 'white' : '#f9fafb'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                    Teléfono
                  </label>
                  <input
                    type="tel"
                    name="phone"
                    value={profileData.phone}
                    onChange={handleInputChange}
                    disabled={!editing}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.5rem',
                      background: editing ? 'white' : '#f9fafb'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                    Dirección
                  </label>
                  <input
                    type="text"
                    name="address"
                    value={profileData.address}
                    onChange={handleInputChange}
                    disabled={!editing}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.5rem',
                      background: editing ? 'white' : '#f9fafb'
                    }}
                  />
                </div>
                <UserEditForm
                  initialData={profileData}
                  editing={editing}
                  onCancel={() => setEditing(false)}
                  onSave={handleSaveProfile}
                />
              </div>

              {/* Biografía */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                  Biografía
                </label>
                <textarea
                  name="bio"
                  value={profileData.bio}
                  onChange={handleInputChange}
                  disabled={!editing}
                  rows={4}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.5rem',
                    background: editing ? 'white' : '#f9fafb',
                    resize: 'vertical'
                  }}
                />
              </div>

              {/* Información profesional (solo para profesionales) */}
              {isProfessional() && (
                <>
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
                    gap: '1rem'
                  }}>
                    <div>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                        Experiencia
                      </label>
                      <input
                        type="text"
                        name="experience"
                        value={profileData.experience}
                        onChange={handleInputChange}
                        disabled={!editing}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.5rem',
                          background: editing ? 'white' : '#f9fafb'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                        Rango de Precios
                      </label>
                      <input
                        type="text"
                        name="priceRange"
                        value={profileData.priceRange}
                        onChange={handleInputChange}
                        disabled={!editing}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.5rem',
                          background: editing ? 'white' : '#f9fafb'
                        }}
                      />
                    </div>
                    <div>
                      <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                        Disponibilidad
                      </label>
                      <input
                        type="text"
                        name="availability"
                        value={profileData.availability}
                        onChange={handleInputChange}
                        disabled={!editing}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.5rem',
                          background: editing ? 'white' : '#f9fafb'
                        }}
                      />
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600', color: '#374151' }}>
                      Servicios Ofrecidos
                    </label>
                    <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                      {profileData.services.map((service, index) => (
                        <span
                          key={index}
                          style={{
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            color: 'white',
                            padding: '0.5rem 1rem',
                            borderRadius: '9999px',
                            fontSize: '0.875rem'
                          }}
                        >
                          {service}
                        </span>
                      ))}
                    </div>
                  </div>
                </>
              )}

              {editing && (
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
                  <button
                    onClick={() => setEditing(false)}
                    style={{
                      padding: '1rem 2rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.5rem',
                      background: 'white',
                      color: '#374151',
                      cursor: 'pointer'
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    onClick={handleSaveProfile}
                    style={{
                      padding: '1rem 2rem',
                      border: 'none',
                      borderRadius: '0.5rem',
                      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                      color: 'white',
                      cursor: 'pointer',
                      fontWeight: '600'
                    }}
                  >
                    Guardar Cambios
                  </button>
                </div>
              )}
            </div>
          </div>
        )}

        {activeTab === 'stats' && (
          <div style={{
            background: 'white',
            borderRadius: '0.75rem',
            padding: '2rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h2 style={{ marginBottom: '2rem', color: '#1f2937' }}>
              {isProfessional() ? 'Estadísticas Profesionales' : 'Mi Actividad'}
            </h2>
            
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
              gap: '1.5rem'
            }}>
              {Object.entries(stats).map(([key, value]) => (
                <div
                  key={key}
                  style={{
                    background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
                    padding: '1.5rem',
                    borderRadius: '0.75rem',
                    textAlign: 'center'
                  }}
                >
                  <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '0.5rem' }}>
                    {value}
                  </div>
                  <div style={{ color: '#6b7280', fontSize: '0.875rem' }}>
                    {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {activeTab === 'history' && (
          <div style={{
            background: 'white',
            borderRadius: '0.75rem',
            padding: '2rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ color: '#1f2937', margin: 0 }}>
                {isProfessional() ? 'Historial de Trabajos' : 'Historial de Solicitudes'}
              </h2>
              <button
                onClick={() => navigate('/orders')}
                style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  border: 'none',
                  cursor: 'pointer'
                }}
              >
                Ver Todo
              </button>
            </div>
            <p style={{ color: '#6b7280' }}>
              Accede a tu historial completo de {isProfessional() ? 'trabajos realizados' : 'servicios solicitados'}
            </p>
          </div>
        )}

        {activeTab === 'reviews' && (
          <div style={{
            background: 'white',
            borderRadius: '0.75rem',
            padding: '2rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h2 style={{ color: '#1f2937', margin: 0 }}>Reseñas y Calificaciones</h2>
              <button
                onClick={() => navigate('/reviews')}
                style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  border: 'none',
                  cursor: 'pointer'
                }}
              >
                Ver Todas
              </button>
            </div>
            <p style={{ color: '#6b7280' }}>
              {isProfessional() 
                ? 'Revisa las calificaciones que has recibido de tus clientes'
                : 'Revisa y administra las calificaciones que has dado'
              }
            </p>
          </div>
        )}

        {activeTab === 'settings' && (
          <div style={{
            background: 'white',
            borderRadius: '0.75rem',
            padding: '2rem',
            boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
          }}>
            <h2 style={{ marginBottom: '2rem', color: '#1f2937' }}>Configuración de Cuenta</h2>
            
            <div style={{ display: 'grid', gap: '1.5rem' }}>
              <div style={{
                padding: '1.5rem',
                border: '1px solid #e5e7eb',
                borderRadius: '0.5rem'
              }}>
                <h3 style={{ marginBottom: '0.5rem', color: '#1f2937' }}>Notificaciones</h3>
                <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
                  Configura qué notificaciones deseas recibir
                </p>
                <div style={{ display: 'grid', gap: '0.5rem' }}>
                  {[
                    'Nuevos mensajes en chat',
                    'Actualizaciones de pedidos',
                    'Recordatorios de citas',
                    'Promociones y ofertas'
                  ].map(option => (
                    <label key={option} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                      <input type="checkbox" defaultChecked />
                      <span>{option}</span>
                    </label>
                  ))}
                </div>
              </div>

              <div style={{
                padding: '1.5rem',
                border: '1px solid #e5e7eb',
                borderRadius: '0.5rem'
              }}>
                <h3 style={{ marginBottom: '0.5rem', color: '#1f2937' }}>Privacidad</h3>
                <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
                  Controla quién puede ver tu información
                </p>
                <button style={{
                  background: '#f3f4f6',
                  border: '1px solid #d1d5db',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer'
                }}>
                  Configurar Privacidad
                </button>
              </div>

              <div style={{
                padding: '1.5rem',
                border: '1px solid #dc2626',
                borderRadius: '0.5rem',
                background: '#fef2f2'
              }}>
                <h3 style={{ marginBottom: '0.5rem', color: '#dc2626' }}>Zona de Peligro</h3>
                <p style={{ color: '#6b7280', marginBottom: '1rem' }}>
                  Acciones irreversibles para tu cuenta
                </p>
                <button style={{
                  background: '#dc2626',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.5rem',
                  cursor: 'pointer'
                }}>
                  Eliminar Cuenta
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProfilePage;