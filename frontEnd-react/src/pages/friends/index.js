
import { Shell, Link, Separator } from '../../components'
import { useFriendship, useSearchUsers } from '../../hooks'
import { ArrowCircleLeftOutline as Back, XOutline as Cancel, CheckOutline as OK } from '@graywolfai/react-heroicons'

const backdrop = user => {
    const picture = user.picture === 'none' ? "https://media.istockphoto.com/id/517998264/es/vector/hombre-icono-de-usuario.jpg?s=612x612&w=is&k=20&c=qX2mEhSna0SxcxMXXyS8YZRaYBm8itZ9N9Ma1S4Bug4=" : user.picture

    return picture
}

export default function Friends() {
    const {user} = useSearchUsers(localStorage.getItem('user'))
    return <Shell className = 'p-4'>
        {user.content ? <img style = {{ height: '28rem' }}
            src = { backdrop(user.content[0]) }
            alt = { `${user} backdrop` }
            className = 'absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105' /> : null}

        <Link variant = 'primary'
            className = 'rounded-full absolute text-white top-4 left-8 flex items-center pl-2 pr-4 py-2 gap-4'
            to = '/'
        >
            <Back className = 'w-8 h-8'/>
            <span>Volver</span>
        </Link>

        {user.content ? <div className = 'mx-auto max-w-screen-2xl p-8'>
            <Header user = { user.content[0] } />
            <FriendShip user = { user.content[0] } />
        </div> : null}
    </Shell>
}

function Header({ user }) {
    return <header className = 'mt-64 relative flex items-end pb-8 mb-8 '>
        {user.length === 0 ?  null : <><img style = {{ height: "250px" }}
             src = { backdrop(user) }
             alt = { `${ user?.email } poster` }
             className = 'w-64 rounded-full z-20' />
        <hgroup className = 'flex-1'>
            <h1 className = {`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                          text-right text-white text-6xl font-bold
                                          p-8`}>
                { user.name }
            </h1>
            <div className='p-8 flex justify-center space-x-64'>
                <span className='flex'>
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6.75 3v2.25M17.25 3v2.25M3 18.75V7.5a2.25 2.25 0 012.25-2.25h13.5A2.25 2.25 0 0121 7.5v11.25m-18 0A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75m-18 0v-7.5A2.25 2.25 0 015.25 9h13.5A2.25 2.25 0 0121 11.25v7.5" />
                </svg>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.birthday.day}/{user.birthday.month}/{user.birthday.year}  </h1>
                </span>

                <span className='flex'>
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5} stroke="currentColor" className="w-10 h-10">
                        <path strokeLinecap="round" strokeLinejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25S4.5 17.642 4.5 10.5a7.5 7.5 0 1115 0z" />
                    </svg>

                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.country}  </h1>
                </span>

                <span className='flex'>
                    <h1 className={`text-black text-4xl font-boldp-8`}> {user.email}  </h1>
                </span>
            </div>
        </hgroup> </>}
    </header>
}

function FriendShip({ user }) {
    const { friendship, confirmarAmistad, eliminarAmistad } = useFriendship( user.email )

    //primero dividimos las amistades
    const propias = friendship?.length > 0 ? friendship.filter(friend => (friend.confirmed === true) || (friend.user === user.email)) : null //que este confirmada, o que se haya enviado por el usuario
    const solicitudes = friendship?.length > 0 ? friendship.filter(friend => (friend.confirmed === false) && (friend.friend===user.email)) : null //las enviadas por otros usuarios al usuario que esta conectado

    return <div className='mt-12 relative flex flex-col pb-8 mb-8'>

        <h2 className = 'mt-16 font-bold text-2xl'>Solicitudes de amistad</h2>
        <Separator />
        <div className='grid gap-4 grid-cols-3 '>  
            {solicitudes?.map(solicitud => <Solicitud  peticion = {solicitud} confirmarAmistad = {confirmarAmistad} eliminarAmistad = {eliminarAmistad} key={solicitud.id}/>)}
        </div>

        <h2 className = 'mt-16 font-bold text-2xl'>Amigos</h2>
        <Separator />
        <div className='grid gap-4 grid-cols-3 '>  
            {propias?.map(solicitud => <Amistad  amistad = {solicitud} eliminarAmistad = {eliminarAmistad} key={solicitud.id}/>)}
        </div>
        
    </div>
}

function Solicitud({peticion, confirmarAmistad, eliminarAmistad}){
    const {user} = useSearchUsers(peticion.user)

    const handleCancela = e => {
        //se llama al delete de amistades
        eliminarAmistad(peticion.id, localStorage.getItem('user'))
    }

    const handleConfirma = e => {
        //se llama al confirmar de amistades
        confirmarAmistad(peticion.id, localStorage.getItem('user'))
        window.location.reload(false);
    }

    return <>{user.content ? <div className='flex items-center p-4 border' style = {{ boxShadow: "0px 0px 5px 4px rgba(214,214,214,1)" }}>
        <div className='flex-1 flex'>
            <Link variant = 'plain-secondary' className = 'ml-4' to = {`/profile/${peticion.user}`}>
                <h1 style={{fontSize: "18px"}}>{user.content[0].name}</h1>
            </Link>
        </div>
        <div className='flex gap-2'>
            <Cancel className = 'w-8 h-8 cursor-pointer' onClick={handleCancela}/>
            <OK className='w-8 h-8 cursor-pointer ' onClick={handleConfirma}/>
        </div>
        
    </div> : null}</>
}

function Amistad({ amistad,  eliminarAmistad }){
    //tenemos que saber que usuario buscar, tenemos que buscar al otro, que puede estar en el campo user o friend habrá que comprobarl
    const {user} = useSearchUsers(amistad.user === localStorage.getItem('user') ? amistad.friend : amistad.user)

    const handleCancela = e => {
        //se llama al delete de amistades
        eliminarAmistad(amistad.id, localStorage.getItem('user'))
    }

    return <>
        {user.content ? amistad?.confirmed === true ? 
        <div className='flex items-center  p-4 border' style = {{ boxShadow: "0px 0px 5px 4px rgba(214,214,214,1)" }}>
            <img style = {{ height: "80px" }} src = { backdrop(user.content[0]) }
                alt = { `${ user.content[0].email } poster` }
                className = 'w-24 rounded-full' />
            <div className='flex-1 flex flex-col items-center mr-4'>
                <h1 style={{fontSize: "32px"}} className="w-full text-center font-bold "> {user.content[0].name} </h1>
                <div className='flex items-center gap-8 pt-4 pb-4'>
                    <h2 className='w-44 text-center italic' >Sois amigos desde el {amistad.since.day}/{amistad.since.month}/{amistad.since.year} </h2>
                    <Cancel className = 'w-8 h-8 cursor-pointer' onClick={handleCancela}/>
                </div>
            </div>
        </div> : 
        <div className='flex items-center  p-4 border' style = {{ boxShadow: "0px 0px 5px 4px rgba(214,214,214,1)" }}>
        <img style = {{ height: "80px" }} src = { backdrop(user.content[0]) }
            alt = { `${ user.content[0].email } poster` }
            className = 'w-24 rounded-full' />
        <div className='flex-1 flex flex-col items-center mr-4'>
            <h1 style={{fontSize: "32px"}} className="w-full text-center font-bold "> {user.content[0].name} </h1>
            <div className='flex items-center gap-8 pt-4 pb-4'>
                <h2 className='w-44 text-center italic  ' >Pendiente de aprobación. </h2>
                <Cancel className = 'w-8 h-8 cursor-pointer' onClick={handleCancela}/>
            </div>
        </div>
    </div> : null}
    </>
}